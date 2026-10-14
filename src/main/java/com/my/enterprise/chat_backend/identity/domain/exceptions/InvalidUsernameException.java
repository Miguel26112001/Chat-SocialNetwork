package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** El username no cumple el formato permitido (3-50, [a-zA-Z0-9._-]). */
public class InvalidUsernameException extends DomainException {

    public InvalidUsernameException(String message) {
        super(message);
    }
}
