package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición de cambio de contraseña.
 */
@Schema(description = "Cambio de contraseña de la propia cuenta (requiere la actual)")
public record ChangePasswordRequest(

        @Schema(description = "Contraseña actual", format = "password")
        @NotBlank(message = "La contraseña actual es obligatoria")
        String currentPassword,

        @Schema(description = "Contraseña nueva (mínimo 8 caracteres, al menos una letra y un dígito)",
                example = "nuevaClave456", format = "password")
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
        String newPassword) {
}
