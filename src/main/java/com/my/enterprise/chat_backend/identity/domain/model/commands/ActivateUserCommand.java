package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/** Intención de reactivar una cuenta desactivada. */
public record ActivateUserCommand(UUID userId) {
}
