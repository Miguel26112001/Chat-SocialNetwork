package com.my.enterprise.chat_backend.identity.domain.model.valueobjects;

import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidUsernameException;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Nombre de usuario único.
 * <p>
 * Reglas de negocio (especificación: tabla users.username):
 *  - entre 3 y 50 caracteres;
 *  - solo letras, dígitos, punto y guion bajo (sin espacios);
 *  - case-sensitive: "Ana" y "ana" son usuarios distintos.
 * <p>
 * Mapeado JPA: embeddable de un solo valor (columna VARCHAR(50)).
 */
@Embeddable
public record Username(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    public Username {
        if (value == null || value.isBlank()) {
            throw new InvalidUsernameException("El nombre de usuario no puede estar vacío");
        }
        String normalized = value.trim();
        if (!PATTERN.matcher(normalized).matches()) {
            throw new InvalidUsernameException(
                    "El nombre de usuario debe tener entre 3 y 50 caracteres y contener solo letras, dígitos, '.' o '_'");
        }
        value = normalized;
    }

    public static Username of(String value) {
        return new Username(value);
    }

    /** Versión en minúsculas, únicamente para búsquedas no sensibles a mayúsculas. */
    public String asSearchKey() {
        return value.toLowerCase(Locale.ROOT);
    }
}
