package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Recurso de salida: una sesión activa ("mis dispositivos").
 * Muestra id y ventanas de validez, NUNCA el token ni su hash.
 */
@Schema(description = "Sesión activa del usuario")
public record SessionResource(

        @Schema(description = "Id de la sesión")
        UUID id,

        @Schema(description = "Fecha de creación de la sesión")
        Instant createdAt,

        @Schema(description = "Fecha de expiración de la sesión")
        Instant expiresAt) {

    public static SessionResource from(RefreshToken token) {
        return new SessionResource(token.getId(), token.getCreatedAt(), token.getExpiresAt());
    }
}
