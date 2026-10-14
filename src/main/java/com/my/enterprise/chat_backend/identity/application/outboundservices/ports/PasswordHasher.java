package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.PasswordHash;

/**
 * Puerto de hashing de contraseñas (Bcrypt / Argon2 en infraestructura).
 * <p>
 * El dominio nunca ve la contraseña en texto plano: la aplicación
 * valida la política, delega el hash a este puerto y entrega al
 * agregado un {@link PasswordHash} ya irreversible.
 */
public interface PasswordHasher {

    PasswordHash hash(String rawPassword);

    boolean matches(String rawPassword, PasswordHash passwordHash);
}
