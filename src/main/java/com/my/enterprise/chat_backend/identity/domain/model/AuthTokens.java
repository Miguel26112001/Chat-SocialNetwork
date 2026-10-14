package com.my.enterprise.chat_backend.identity.domain.model;

import java.time.Instant;

/**
 * Par de tokens emitido tras un login o una renovación de sesión.
 * <p>
 * Es el RESULTADO del caso de uso de autenticación: un record de
 * dominio inmutable que cruza la capa de aplicación hacia la capa de
 * interfaces. El refresh token viaja en texto plano UNA sola vez
 * (aquí); en la base de datos solo queda su hash.
 */
public record AuthTokens(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {
}
