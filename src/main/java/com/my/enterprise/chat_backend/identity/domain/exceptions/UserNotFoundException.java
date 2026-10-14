package com.my.enterprise.chat_backend.identity.domain.exceptions;

import java.util.UUID;

/** No existe ningún usuario con ese id. */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UUID userId) {
        super("No existe ningún usuario con id '" + userId + "'");
    }

    public UserNotFoundException(String identifier) {
        super("No existe ningún usuario identificado por '" + identifier + "'");
    }
}
