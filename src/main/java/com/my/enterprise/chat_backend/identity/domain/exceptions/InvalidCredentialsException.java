package com.my.enterprise.chat_backend.identity.domain.exceptions;

/**
 * Credenciales inválidas: usuario/contraseña incorrectos, token de
 * sesión inválido, expirado o revocado.
 *
 * Intencionadamente NO distingue entre "no existe el usuario" y
 * "contraseña incorrecta": revelar la diferencia facilita el
 * user enumeration.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
