package com.my.enterprise.chat_backend.identity.domain.model.events;

import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: una cuenta fue creada.
 *
 * Es el más importante del sistema: otros bounded contextos lo
 * consumen para crear su propia parte del usuario, p. ej. User Profile
 * crea su perfil (display_name = username por defecto).
 */
public record UserRegisteredEvent(
        UUID userId,
        Username username,
        Email email,
        Instant occurredOn) {
}
