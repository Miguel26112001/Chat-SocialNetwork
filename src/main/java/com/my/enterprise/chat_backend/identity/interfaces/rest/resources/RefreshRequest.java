package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de renovación de sesión.
 */
@Schema(description = "Refresh token vigente para intercambiar por un par nuevo (rotación)")
public record RefreshRequest(

        @Schema(description = "Refresh token en texto plano recibido al hacer login o refresh")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken) {
}
