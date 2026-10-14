package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Recurso de salida: proyección pública de una cuenta.
 * Vive en la capa de interfaces: es lo que serializa la API.
 * Nunca expone el hash de la contraseña.
 */
@Schema(description = "Cuenta de usuario")
public record UserResource(

        @Schema(description = "Id de la cuenta", example = "7c4ad599-cffc-4fee-b87e-81ce86d78f79")
        UUID id,

        @Schema(description = "Nombre de usuario único", example = "ana_dev")
        String username,

        @Schema(description = "Correo electrónico", example = "ana@example.com")
        String email,

        @Schema(description = "Si la cuenta está activa", example = "true")
        boolean active,

        @Schema(description = "Rol de la cuenta", example = "USER")
        UserRole role,

        @Schema(description = "Fecha de creación")
        Instant createdAt,

        @Schema(description = "Fecha de última modificación")
        Instant updatedAt) {
}
