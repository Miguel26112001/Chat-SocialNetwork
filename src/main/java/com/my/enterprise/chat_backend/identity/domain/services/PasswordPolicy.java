package com.my.enterprise.chat_backend.identity.domain.services;

import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidPasswordException;

/**
 * Servicio de dominio: política de contraseñas.
 * <p>
 * Contiene reglas que no pertenecen a un agregado concreto sino al
 * contexto Identity en su conjunto. Es puro (sin puertos ni
 * infraestructura): válida la contraseña en texto plano ANTES de que
 * la aplicación la envíe al puerto {@code PasswordHasher}.
 * <p>
 * Reglas actuales:
 *  - entre 8 y 72 caracteres (72 = límite práctico de Bcrypt);
 *  - al menos una letra y un dígito;
 *  - sin espacios al inicio o al final.
 */
public class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidPasswordException("La contraseña no puede estar vacía");
        }
        String password = rawPassword.strip();
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new InvalidPasswordException(
                    "La contraseña debe tener entre " + MIN_LENGTH + " y " + MAX_LENGTH + " caracteres");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new InvalidPasswordException("La contraseña debe contener al menos una letra y un dígito");
        }
    }
}
