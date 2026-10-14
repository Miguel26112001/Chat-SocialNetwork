package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Puerto de emisión y validación de access tokens JWT.
 * La implementación (firma, expiración, claims) vive en infraestructura.
 */
public interface TokenService {

    /**
     * Emite un access token JWT firmado para el usuario.
     * Incluye el claim {@code role} para autorización sin consulta a BD.
     */
    IssuedAccessToken issueAccessToken(User user);

    /**
     * Valida un access token y devuelve sus claims (id y rol).
     * El rol es una foto al momento de emisión (≤ TTL de expiración);
     * las operaciones críticas revalidan contra la BD en el servicio.
     * @throws com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException
     *         si el token es inválido, está corrupto o expiró.
     */
    AccessTokenClaims parseClaims(String accessToken);

    /** Access token emitido con su instante de expiración. */
    record IssuedAccessToken(String token, Instant expiresAt) {
    }

    /** Claims de un access token válido: quién eres y con qué rol. */
    record AccessTokenClaims(UUID userId, UserRole role) {
    }
}
