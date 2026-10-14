package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import com.my.enterprise.chat_backend.identity.domain.model.AuthTokens;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Recurso de salida: par de tokens tras login o renovación.
 * El refresh token se entrega en texto plano UNA sola vez (aquí);
 * en la base de datos solo queda su hash.
 */
@Schema(description = "Par de tokens de sesión")
public record AuthTokensResource(

        @Schema(description = "Access token JWT (usar como 'Bearer <token>')")
        String accessToken,

        @Schema(description = "Refresh token para renovar la sesión (guardarlo seguro)")
        String refreshToken,

        @Schema(description = "Expiración del access token")
        Instant accessTokenExpiresAt,

        @Schema(description = "Expiración del refresh token")
        Instant refreshTokenExpiresAt) {

    public static AuthTokensResource from(AuthTokens tokens) {
        return new AuthTokensResource(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshTokenExpiresAt());
    }
}
