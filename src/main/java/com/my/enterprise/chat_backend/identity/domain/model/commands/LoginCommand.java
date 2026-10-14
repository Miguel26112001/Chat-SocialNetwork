package com.my.enterprise.chat_backend.identity.domain.model.commands;

/**
 * Intención de iniciar sesión.
 * Acepta username O email (el CommandService decide según el formato).
 */
public record LoginCommand(
        String usernameOrEmail,
        String rawPassword) {
}
