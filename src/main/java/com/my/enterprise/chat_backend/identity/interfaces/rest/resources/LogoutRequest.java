package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de logout: el refresh token a revocar.
 */
public record LogoutRequest(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken) {
}
