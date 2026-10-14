package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/**
 * Intención de cambiar la contraseña de la cuenta del usuario.
 * Requiere la contraseña actual: el CommandService la verificará
 * antes de aplicar el cambio.
 */
public record ChangePasswordCommand(
        UUID userId,
        String currentRawPassword,
        String newRawPassword) {
}
