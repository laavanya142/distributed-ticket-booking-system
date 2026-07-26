package com.ticketbooking.seat.infrastructure.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Component responsible exclusively for high-concurrency atomic seat locking operations in Redis.
 */
@Component
public class SeatLockManager {

    private final RedisTemplate<String, String> redisTemplate;

    @SuppressWarnings("rawtypes")
    private final RedisScript<List> lockSeatsScript;

    @SuppressWarnings("rawtypes")
    private final RedisScript<List> releaseSeatsScript;

    private final long defaultTtlSeconds;

    /**
     * Constructs SeatLockManager with Redis dependencies and configurable TTL.
     *
     * @param redisTemplate Redis template for string operations.
     * @param lockSeatsScript Redis Lua script for multi-seat locking.
     * @param releaseSeatsScript Redis Lua script for multi-seat unlock.
     * @param defaultTtlSeconds Configurable default lock TTL in seconds.
     */
    @SuppressWarnings("rawtypes")
    public SeatLockManager(
            RedisTemplate<String, String> redisTemplate,
            RedisScript<List> lockSeatsScript,
            RedisScript<List> releaseSeatsScript,
            @Value("${seat.lock.ttl-seconds:600}") long defaultTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.lockSeatsScript = lockSeatsScript;
        this.releaseSeatsScript = releaseSeatsScript;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    /**
     * Atomically locks a batch of show seats in Redis using a Lua script.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat identifiers to lock.
     * @param lockToken Unique lock token (e.g. booking session ID).
     * @param userId Owning user identifier.
     * @param ttlSeconds Lock duration in seconds (if 0 or negative, defaultTtlSeconds is used).
     * @return true if all seats were successfully locked; false if any seat was already locked.
     */
    public boolean lockSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId, long ttlSeconds) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return true;
        }

        long ttl = ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds;
        List<String> keys = buildLockKeys(showId, showSeatIds);
        String lockValue = buildLockValue(lockToken, userId);

        List<?> result = redisTemplate.execute(lockSeatsScript, keys, lockValue, String.valueOf(ttl));

        if (result != null && !result.isEmpty() && result.get(0) instanceof Long longVal) {
            return longVal == 1L;
        }
        return false;
    }

    /**
     * Atomically releases a batch of show seats in Redis if all are owned by the lock token and user ID.
     *
     * @param showId Show identifier.
     * @param showSeatIds List of show seat identifiers to release.
     * @param lockToken Lock token verifying ownership.
     * @param userId User identifier verifying ownership.
     * @return true if all seats were released; false if any lock token or user ID mismatched.
     */
    public boolean releaseSeats(UUID showId, List<UUID> showSeatIds, UUID lockToken, UUID userId) {
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            return true;
        }

        List<String> keys = buildLockKeys(showId, showSeatIds);
        String lockValue = buildLockValue(lockToken, userId);
        List<?> result = redisTemplate.execute(releaseSeatsScript, keys, lockValue);

        if (result != null && !result.isEmpty() && result.get(0) instanceof Long longVal) {
            return longVal == 1L;
        }
        return false;
    }

    /**
     * Verifies whether a specific show seat is locked in Redis by the specified lock token and user ID.
     *
     * @param showId Show identifier.
     * @param showSeatId Show seat identifier.
     * @param lockToken Lock token to verify.
     * @param userId User identifier to verify.
     * @return true if seat is locked by the specified token and user ID.
     */
    public boolean verifyLock(UUID showId, UUID showSeatId, UUID lockToken, UUID userId) {
        String key = buildSingleLockKey(showId, showSeatId);
        String currentToken = redisTemplate.opsForValue().get(key);
        String expectedValue = buildLockValue(lockToken, userId);
        return Objects.equals(currentToken, expectedValue);
    }

    /**
     * Gets the default lock TTL in seconds.
     *
     * @return Configured TTL in seconds.
     */
    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    private List<String> buildLockKeys(UUID showId, List<UUID> showSeatIds) {
        List<String> keys = new ArrayList<>(showSeatIds.size());
        for (UUID showSeatId : showSeatIds) {
            keys.add(buildSingleLockKey(showId, showSeatId));
        }
        return keys;
    }

    private String buildSingleLockKey(UUID showId, UUID showSeatId) {
        return String.format("show:%s:seat:%s:lock", showId, showSeatId);
    }

    private String buildLockValue(UUID lockToken, UUID userId) {
        return lockToken.toString() + ":" + userId.toString();
    }
}
