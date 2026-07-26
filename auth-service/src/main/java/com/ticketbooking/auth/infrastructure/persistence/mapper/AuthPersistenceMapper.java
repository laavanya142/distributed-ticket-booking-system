package com.ticketbooking.auth.infrastructure.persistence.mapper;

import com.ticketbooking.auth.domain.model.RefreshToken;
import com.ticketbooking.auth.domain.model.Role;
import com.ticketbooking.auth.domain.model.UserCredential;
import com.ticketbooking.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.ticketbooking.auth.infrastructure.persistence.entity.UserCredentialJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper between clean architecture domain entities and JPA entities.
 */
@Mapper(componentModel = "spring")
public interface AuthPersistenceMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    UserCredentialJpaEntity toJpaEntity(UserCredential domain);

    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    UserCredential toDomain(UserCredentialJpaEntity entity);

    RefreshTokenJpaEntity toJpaEntity(RefreshToken domain);

    RefreshToken toDomain(RefreshTokenJpaEntity entity);

    @Named("roleToString")
    default String roleToString(Role role) {
        return role != null ? role.name() : null;
    }

    @Named("stringToRole")
    default Role stringToRole(String roleStr) {
        return roleStr != null ? Role.valueOf(roleStr) : null;
    }
}
