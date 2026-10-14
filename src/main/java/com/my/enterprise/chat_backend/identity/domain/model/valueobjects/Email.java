package com.my.enterprise.chat_backend.identity.domain.model.valueobjects;

import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidEmailException;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo electrónico del usuario.
 * <p>
 * Reglas de negocio (especificación: tabla users.email):
 *  - máximo 255 caracteres;
 *  - formato válido;
 *  - se normaliza a minúsculas para que el índice UNIQUE
 *    evite duplicados como "Ana@x.com" y "ana@x.com".
 * <p>
 * Mapeado JPA: embeddable de un solo valor (columna VARCHAR(255)).
 */
@Embeddable
public record Email(String value) {

    private static final int MAX_LENGTH = 255;
    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("El correo electrónico no puede estar vacío");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidEmailException("El correo electrónico no puede superar " + MAX_LENGTH + " caracteres");
        }
        if (!PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException("El correo electrónico '" + value + "' no tiene un formato válido");
        }
        value = normalized;
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
