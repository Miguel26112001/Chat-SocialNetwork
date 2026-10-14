package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** Ya existe un usuario registrado con ese username (users.username UNIQUE). */
public class UsernameAlreadyExistsException extends DomainException {

    public UsernameAlreadyExistsException(String username) {
        super("Ya existe un usuario con el nombre '" + username + "'");
    }
}
