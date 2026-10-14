package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Puerto de emisión y validación de access tokens JWT.
 * La implementación (firma, expiración, claims) vive en infraestructura.
 */
public interface TokenService {

    /**
     * Emite un access token JWT firmado para el usuario.
     */
    IssuedAccessToken issueAccessToken(User user);

    /**
     * Valida un access token y devuelve el id del usuario.
     * @throws com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException
     *         si el token es inválido, está corrupto o expiró.
     */
    UUID parseUserId(String accessToken);

    /** Access token emitido con su instante de expiración. */
    record IssuedAccessToken(String token, Instant expiresAt) {
    }
}
