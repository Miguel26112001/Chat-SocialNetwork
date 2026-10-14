package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/**
 * Intención de promover una cuenta a ADMIN.
 * Solo un admin puede promover (lo verifica el servicio).
 * Incluye quién lo solicita para auditar la operación.
 */
public record PromoteUserCommand(
        UUID targetUserId,
        UUID requestedBy) {
}
