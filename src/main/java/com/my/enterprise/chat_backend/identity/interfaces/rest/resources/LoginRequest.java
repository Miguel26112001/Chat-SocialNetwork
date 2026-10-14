package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de login: acepta username O email.
 */
public record LoginRequest(

        @NotBlank(message = "El identificador es obligatorio")
        String usernameOrEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
