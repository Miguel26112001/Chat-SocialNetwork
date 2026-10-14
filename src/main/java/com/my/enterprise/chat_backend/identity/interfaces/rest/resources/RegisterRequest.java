package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición de registro.
 * La validación de formato detallada la hacen los value objects del
 * dominio; aquí solo se valida la forma básica del transporte.
 */
@Schema(description = "Datos para crear una cuenta nueva")
public record RegisterRequest(

        @Schema(description = "Nombre de usuario único (3-50, letras, dígitos, '.' y '_')",
                example = "ana_dev")
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        String username,

        @Schema(description = "Correo electrónico único", example = "ana@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede superar 255 caracteres")
        String email,

        @Schema(description = "Contraseña en texto plano (mínimo 8 caracteres, al menos una letra y un dígito; "
                + "se almacena solo su hash BCrypt)",
                example = "secret123", format = "password")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password) {
}
