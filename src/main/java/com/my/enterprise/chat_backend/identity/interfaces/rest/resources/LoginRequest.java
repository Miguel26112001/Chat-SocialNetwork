package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de login: acepta username O email.
 */
@Schema(description = "Credenciales de inicio de sesión (username o email + contraseña)")
public record LoginRequest(

        @Schema(description = "Username o email de la cuenta", example = "ana_dev")
        @NotBlank(message = "El identificador es obligatorio")
        String usernameOrEmail,

        @Schema(description = "Contraseña de la cuenta", example = "secret123", format = "password")
        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
