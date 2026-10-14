package com.my.enterprise.chat_backend.identity.domain.model.valueobjects;

import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidPasswordException;
import jakarta.persistence.Embeddable;

/**
 * Hash de la contraseña (BCrypt / Argon2).
 * <p>
 * REGLA DE ORO: ni este VO ni el agregado User jamás ven la
 * contraseña en texto plano; solo su hash irreversible.
 * El hash lo produce un puerto outbound ({@code PasswordHasher})
 * en la capa de infraestructura.
 * <p>
 * Mapeado JPA: embeddable de un solo valor (VARCHAR(255) = columna
 * users.password_hash).
 */
@Embeddable
public record PasswordHash(String value) {

    private static final int MAX_LENGTH = 255;

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException("El hash de la contraseña no puede estar vacío");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidPasswordException("El hash de la contraseña no puede superar " + MAX_LENGTH + " caracteres");
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
