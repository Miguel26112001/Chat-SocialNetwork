package com.my.enterprise.chat_backend.identity.application.dto;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección inmutable de un usuario para la capa de interfaces.
 * Los DTOs cruzan la frontera del contexto: nunca se devuelve el
 * agregado (ni el hash de la contraseña) al exterior.
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
