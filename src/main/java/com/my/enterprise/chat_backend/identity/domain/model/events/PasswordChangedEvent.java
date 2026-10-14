package com.my.enterprise.chat_backend.identity.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: la contraseña de una cuenta cambió.
 * Se usa para revocar el resto de sesiones (refresh tokens)
 * del usuario, por seguridad.
 */
public record PasswordChangedEvent(
        UUID userId,
        Instant occurredOn) {
}
