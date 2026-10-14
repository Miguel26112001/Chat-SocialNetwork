package com.my.enterprise.chat_backend.identity.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: una cuenta fue desactivada.
 * Social Graph y Engagement lo usan para suspender la actividad
 * asociada; Identity revoca sus refresh tokens.
 */
public record UserDeactivatedEvent(
        UUID userId,
        Instant occurredOn) {
}
