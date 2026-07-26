package com.ticketbooking.auth.infrastructure.persistence.adapter;

import com.ticketbooking.auth.domain.model.UserCredential;
import com.ticketbooking.auth.domain.repository.UserCredentialRepository;
import com.ticketbooking.auth.infrastructure.persistence.entity.UserCredentialJpaEntity;
import com.ticketbooking.auth.infrastructure.persistence.mapper.AuthPersistenceMapper;
import com.ticketbooking.auth.infrastructure.persistence.repository.SpringDataUserCredentialRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Clean Architecture infrastructure adapter implementing UserCredentialRepository.
 */
@Component
@RequiredArgsConstructor
public class UserCredentialRepositoryAdapter implements UserCredentialRepository {

    private final SpringDataUserCredentialRepository jpaRepository;
    private final AuthPersistenceMapper mapper;

    @Override
    public Optional<UserCredential> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<UserCredential> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public UserCredential save(UserCredential userCredential) {
        UserCredentialJpaEntity entity = mapper.toJpaEntity(userCredential);
        UserCredentialJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
