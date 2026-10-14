package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** La cuenta está desactivada (users.is_active = FALSE) y no puede operar. */
public class InactiveUserException extends DomainException {

    public InactiveUserException(String username) {
        super("La cuenta '" + username + "' está desactivada");
    }
}
