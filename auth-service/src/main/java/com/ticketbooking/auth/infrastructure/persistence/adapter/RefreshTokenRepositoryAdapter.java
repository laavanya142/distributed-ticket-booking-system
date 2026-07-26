package com.ticketbooking.auth.infrastructure.persistence.adapter;

import com.ticketbooking.auth.domain.model.RefreshToken;
import com.ticketbooking.auth.domain.repository.RefreshTokenRepository;
import com.ticketbooking.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.ticketbooking.auth.infrastructure.persistence.mapper.AuthPersistenceMapper;
import com.ticketbooking.auth.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Clean Architecture infrastructure adapter implementing RefreshTokenRepository.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository jpaRepository;
    private final AuthPersistenceMapper mapper;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = mapper.toJpaEntity(refreshToken);
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
