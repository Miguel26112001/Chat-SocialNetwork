package com.my.enterprise.chat_backend.identity.domain.model.commands;

/**
 * Intención de renovar la sesión: intercambia un refresh token
 * vigente por un par nuevo de tokens (rotación).
 */
public record RefreshSessionCommand(
        String rawRefreshToken) {
}
