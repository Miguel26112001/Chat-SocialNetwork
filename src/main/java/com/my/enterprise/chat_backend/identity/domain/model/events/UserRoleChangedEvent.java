package com.my.enterprise.chat_backend.identity.domain.model.events;

import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: el rol de una cuenta cambió.
 * Sirve para auditoría y para que otros contextos reaccionen
 * (p. ej. invalidar cachés de autorización).
 */
public record UserRoleChangedEvent(
        UUID userId,
        UserRole newRole,
        Instant occurredOn) {
}
