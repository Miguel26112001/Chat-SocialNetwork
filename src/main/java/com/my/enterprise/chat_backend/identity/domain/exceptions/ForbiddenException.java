package com.my.enterprise.chat_backend.identity.domain.exceptions;

/**
 * La cuenta no tiene privilegios para esta operación (p. ej. se
 * requiere rol ADMIN). Se traduce a HTTP 403.
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(message);
    }
}
