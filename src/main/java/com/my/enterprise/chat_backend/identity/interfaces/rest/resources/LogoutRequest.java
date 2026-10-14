package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de logout: el refresh token a revocar.
 */
@Schema(description = "Refresh token a revocar (cierre de sesión en este dispositivo)")
public record LogoutRequest(

        @Schema(description = "Refresh token en texto plano recibido al hacer login o refresh")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken) {
}
