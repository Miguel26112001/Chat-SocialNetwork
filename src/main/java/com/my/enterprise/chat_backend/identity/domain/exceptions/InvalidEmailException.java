package com.my.enterprise.chat_backend.identity.domain.exceptions;

/** El correo no tiene un formato válido o supera la longitud máxima. */
public class InvalidEmailException extends DomainException {

    public InvalidEmailException(String message) {
        super(message);
    }
}
