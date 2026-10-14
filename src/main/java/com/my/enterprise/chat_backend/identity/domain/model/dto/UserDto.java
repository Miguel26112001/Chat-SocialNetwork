package com.my.enterprise.chat_backend.identity.domain.model.dto;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de dominio: proyección inmutable de una cuenta.
 *
 * Es lo que devuelven los servicios de aplicación en lugar del
 * agregado: el agregado User nunca cruza la frontera de la capa
 * de aplicación. La capa de interfaces lo transforma a
 * {@code UserResource} mediante {@code UserTransform}.
 */
public record UserDto(
        UUID id,
        String username,
        String email,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
