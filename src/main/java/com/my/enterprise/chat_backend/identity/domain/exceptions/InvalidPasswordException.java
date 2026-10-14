package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** La contraseña en texto plano no cumple la política de seguridad. */
public class InvalidPasswordException extends DomainException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
