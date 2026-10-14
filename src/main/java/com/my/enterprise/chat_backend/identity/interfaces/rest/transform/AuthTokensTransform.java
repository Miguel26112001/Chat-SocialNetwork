package com.my.enterprise.chat_backend.identity.interfaces.rest.transform;

import com.my.enterprise.chat_backend.identity.domain.model.dto.AuthTokens;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.AuthTokensResource;

/**
 * Transforma el DTO de dominio (lo que entrega la capa de aplicación)
 * al recurso de la API (lo que serializa la capa de interfaces).
 * Funciones estáticas puras, sin estado ni dependencias.
 */
public final class AuthTokensTransform {

    private AuthTokensTransform() {
    }

    public static AuthTokensResource toResource(AuthTokens dto) {
        return new AuthTokensResource(
                dto.accessToken(),
                dto.refreshToken(),
                dto.accessTokenExpiresAt(),
                dto.refreshTokenExpiresAt());
    }
}
