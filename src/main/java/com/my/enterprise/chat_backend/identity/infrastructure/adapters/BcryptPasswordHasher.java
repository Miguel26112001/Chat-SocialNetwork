package com.my.enterprise.chat_backend.identity.infrastructure.adapters;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.PasswordHasher;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador del puerto {@code PasswordHasher}: BCrypt.
 * <p>
 * BCrypt incluye salt aleatorio por hash, así que dos contraseñas
 * iguales producen hashes distintos. El factor de coste por defecto
 * (10) es el recomendado para 2026.
 */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(String rawPassword) {
        return PasswordHash.of(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash) {
        return encoder.matches(rawPassword, passwordHash.value());
    }
}
