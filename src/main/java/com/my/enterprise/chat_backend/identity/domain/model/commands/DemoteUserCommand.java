package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/**
 * Intención de degradar una cuenta ADMIN a USER común.
 * Solo un admin puede degradar (lo verifica el servicio).
 * Incluye quién lo solicita para auditar la operación.
 */
public record DemoteUserCommand(
        UUID targetUserId,
        UUID requestedBy) {
}
