package com.ticketbooking.seat.application.service;

import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.SeatCategory;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import com.ticketbooking.seat.domain.entity.ShowSeatStatus;
import com.ticketbooking.seat.domain.exception.InvalidSeatLockException;
import com.ticketbooking.seat.domain.exception.SeatAlreadyLockedException;
import com.ticketbooking.seat.domain.exception.SeatNotFoundException;
import com.ticketbooking.seat.domain.repository.SeatRepository;
import com.ticketbooking.seat.domain.repository.ShowSeatRepository;
import com.ticketbooking.seat.infrastructure.redis.SeatLockManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation managing seat inventory master data and high-concurrency seat locking.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private static final int MAX_LOCK_BATCH_SIZE = 10;
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal PREMIUM_MULTIPLIER = new BigDecimal("1.20");

    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockManager seatLockManager;

    @Override
    @Transactional
    public List<Seat> createScreenSeats(UUID screenId, List<Seat> seats) {
        log.info("Batch creating {} physical seats for screen ID: {}", seats.size(), screenId);
        List<Seat> toSave = new ArrayList<>(seats.size());
        for (Seat seat : seats) {
            seat.setScreenId(screenId);
            if (!seatRepository.existsByScreenIdAndRowNumberAndSeatNumber(
                    screenId, seat.getRowNumber(), seat.getSeatNumber())) {
                toSave.add(seat);
            }
        }
        return seatRepository.saveAll(toSave);
    }

    @Override
    @Transactional
    public int initializeShowSeats(UUID showId, UUID screenId, BigDecimal basePrice) {
        log.info(
                "Initializing show seats for show ID: {} on screen ID: {} with base price: {}",
                showId,
                screenId,
                basePrice);
        List<Seat> activeSeats = seatRepository.findByScreenIdAndActiveTrue(screenId);
        if (activeSeats.isEmpty()) {
            log.warn("No active physical seats found for screen ID: {}", screenId);
            return 0;
        }

        List<ShowSeat> showSeats = new ArrayList<>(activeSeats.size());
        for (Seat seat : activeSeats) {
            BigDecimal price = calculateSeatPrice(basePrice, seat.getCategory());
            ShowSeat showSeat = ShowSeat.builder()
                    .showId(showId)
                    .seat(seat)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(price)
                    .build();
            showSeats.add(showSeat);
        }

        List<ShowSeat> saved = showSeatRepository.saveAll(showSeats);
        log.info("Successfully initialized {} show seats for show ID: {}", saved.size(), showId);
        return saved.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeat> getShowSeatMap(UUID showId) {
        log.debug("Fetching show seat map for show ID: {}", showId);
        return showSeatRepository.findByShowIdWithSeat(showId);
    }

    @Override
    @Transactional
    public List<ShowSeat> lockSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId, long ttlSeconds) {
        log.info(
                "Attempting to lock {} seats for show ID: {} with lock token: {} and user ID: {}",
                showSeatIds.size(),
                showId,
                lockToken,
                userId);

        if (showSeatIds.isEmpty() || showSeatIds.size() > MAX_LOCK_BATCH_SIZE) {
            throw new IllegalArgumentException("Seat lock batch size must be between 1 and " + MAX_LOCK_BATCH_SIZE);
        }

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndIdInWithSeat(showId, showSeatIds);
        if (showSeats.size() != showSeatIds.size()) {
            throw new SeatNotFoundException(showSeatIds.get(0));
        }

        Instant now = Instant.now();
        long ttl = ttlSeconds > 0 ? ttlSeconds : seatLockManager.getDefaultTtlSeconds();

        List<UUID> conflictingSeatIds = new ArrayList<>();
        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == ShowSeatStatus.BOOKED) {
                conflictingSeatIds.add(ss.getId());
            } else if (ss.getStatus() == ShowSeatStatus.LOCKED && !ss.isLockExpired(now, ttl)) {
                conflictingSeatIds.add(ss.getId());
            }
        }

        if (!conflictingSeatIds.isEmpty()) {
            log.warn("Lock attempt failed due to conflicting seats in show ID {}: {}", showId, conflictingSeatIds);
            throw new SeatAlreadyLockedException(showId, conflictingSeatIds);
        }

        boolean redisLocked = seatLockManager.lockSeats(showId, showSeatIds, lockToken, userId, ttlSeconds);
        if (!redisLocked) {
            log.warn("Redis atomic lock failed for show ID: {} and seats: {}", showId, showSeatIds);
            throw new SeatAlreadyLockedException(showId, showSeatIds);
        }

        try {
            for (ShowSeat ss : showSeats) {
                ss.lock(lockToken, userId, now);
            }
            List<ShowSeat> saved = showSeatRepository.saveAll(showSeats);
            log.info("Successfully locked {} seats in database for show ID: {}", saved.size(), showId);
            return saved;
        } catch (Exception ex) {
            log.error("Database lock update failed for show ID: {}. Executing compensating Redis release.", showId, ex);
            seatLockManager.releaseSeats(showId, showSeatIds, lockToken, userId);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void releaseSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        log.info(
                "Releasing {} locked seats for show ID: {} with lock token: {} and user ID: {}",
                showSeatIds.size(),
                showId,
                lockToken,
                userId);

        if (showSeatIds.isEmpty()) {
            return;
        }

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndIdInWithSeat(showId, showSeatIds);

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == ShowSeatStatus.LOCKED) {
                if ((ss.getLockToken() != null && !Objects.equals(ss.getLockToken(), lockToken))
                        || (ss.getLockedByUserId() != null && !Objects.equals(ss.getLockedByUserId(), userId))) {
                    throw new InvalidSeatLockException(lockToken);
                }
                ss.unlock();
            }
        }

        showSeatRepository.saveAll(showSeats);
        seatLockManager.releaseSeats(showId, showSeatIds, lockToken, userId);
        log.info("Successfully released seats for show ID: {}", showId);
    }

    @Override
    @Transactional
    public void confirmSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        log.info(
                "Confirming {} show seats for show ID: {} with lock token: {} and user ID: {}",
                showSeatIds.size(),
                showId,
                lockToken,
                userId);

        if (showSeatIds.isEmpty()) {
            return;
        }

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndIdInWithSeat(showId, showSeatIds);
        if (showSeats.size() != showSeatIds.size()) {
            throw new SeatNotFoundException(showSeatIds.get(0));
        }

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() != ShowSeatStatus.LOCKED
                    || !Objects.equals(ss.getLockToken(), lockToken)
                    || !Objects.equals(ss.getLockedByUserId(), userId)) {
                log.warn("Confirm seats failed validation for show ID: {} and seat ID: {}", showId, ss.getId());
                throw new InvalidSeatLockException(lockToken);
            }
        }

        for (ShowSeat ss : showSeats) {
            ss.confirmBooking();
        }
        showSeatRepository.saveAll(showSeats);
        seatLockManager.releaseSeats(showId, showSeatIds, lockToken, userId);
        log.info("Successfully confirmed {} seats as BOOKED for show ID: {}", showSeats.size(), showId);
    }

    private BigDecimal calculateSeatPrice(BigDecimal basePrice, SeatCategory category) {
        if (category == SeatCategory.VIP) {
            return basePrice.multiply(VIP_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        } else if (category == SeatCategory.PREMIUM) {
            return basePrice.multiply(PREMIUM_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }
}
