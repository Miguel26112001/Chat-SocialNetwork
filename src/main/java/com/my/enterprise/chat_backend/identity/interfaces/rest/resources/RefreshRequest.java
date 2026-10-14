package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de renovación de sesión.
 */
public record RefreshRequest(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken) {
}
