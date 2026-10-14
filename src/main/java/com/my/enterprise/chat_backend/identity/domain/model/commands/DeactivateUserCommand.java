package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/** Intención de desactivar una cuenta (soft delete de users.is_active). */
public record DeactivateUserCommand(UUID userId) {
}
