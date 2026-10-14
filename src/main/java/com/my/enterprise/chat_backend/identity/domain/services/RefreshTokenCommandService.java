package com.my.enterprise.chat_backend.identity.domain.services;

import com.my.enterprise.chat_backend.identity.domain.model.dto.AuthTokens;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LoginCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LogoutCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RefreshSessionCommand;

/**
 * Contrato de los comandos (lado WRITE, CQRS) del agregado RefreshToken.
 *
 * El ciclo de vida de las sesiones vive aquí, no en
 * {@link UserCommandService}: login emite un refresh token, logout lo
 * revoca y refreshSession lo rota.
 *
 * Interfaz en la capa de dominio; la implementación vive en la capa
 * de aplicación (application.commandservices).
 */
public interface RefreshTokenCommandService {

    /**
     * Inicia sesión: autentica al usuario y emite un par de tokens
     * (access JWT + refresh token).
     */
    AuthTokens login(LoginCommand command);

    /**
     * Cierra sesión en un dispositivo: revoca el refresh token recibido.
     * Idempotente: si el token ya no existe, no hace nada.
     */
    void logout(LogoutCommand command);

    /**
     * Renueva la sesión: intercambia un refresh token vigente por un
     * par nuevo (rotación).
     */
    AuthTokens refreshSession(RefreshSessionCommand command);
}
