package com.my.enterprise.chat_backend.identity.domain.model.dto;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de dominio: proyección inmutable de una sesión activa.
 * <p>
 * Es lo que devuelve el servicio de aplicación en lugar del agregado:
 * muestra id y ventanas de validez, NUNCA el token ni su hash.
 * La capa de interfaces lo transforma a {@code SessionResource}
 * mediante {@code SessionTransform}.
 */
public record SessionDto(
        UUID id,
        Instant createdAt,
        Instant expiresAt) {

    public static SessionDto from(RefreshToken token) {
        return new SessionDto(token.getId(), token.getCreatedAt(), token.getExpiresAt());
    }
}
