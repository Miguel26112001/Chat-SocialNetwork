package com.my.enterprise.chat_backend.identity.application.dto;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección inmutable de una sesión activa (refresh token vigente).
 * Para el caso de uso "mis dispositivos": muestra id y ventanas de
 * validez, NUNCA el token ni su hash.
 */
public record SessionDto(
        UUID id,
        Instant createdAt,
        Instant expiresAt) {

    public static SessionDto from(RefreshToken token) {
        return new SessionDto(token.getId(), token.getCreatedAt(), token.getExpiresAt());
    }
}
