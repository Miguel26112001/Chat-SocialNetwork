package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** Ya existe un usuario registrado con ese correo (users.email UNIQUE). */
public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String email) {
        super("Ya existe una cuenta con el correo '" + email + "'");
    }
}
