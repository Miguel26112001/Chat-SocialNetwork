package com.my.enterprise.chat_backend.identity.domain.model.commands;

import java.util.UUID;

/**
 * Intención de cerrar sesión en un dispositivo: revoca el refresh
 * token recibido (logout). Si el token ya no existe, no hace nada.
 */
public record LogoutCommand(
        UUID userId,
        String rawRefreshToken) {
}
