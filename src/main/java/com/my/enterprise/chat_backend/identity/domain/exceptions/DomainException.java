package com.my.enterprise.chat_backend.identity.domain.exceptions;

/**
 * Raíz de todas las excepciones de dominio del contexto Identity.
 * Una violación de una regla de negocio siempre se expresa como una
 * excepción de dominio, nunca como {@code null}, booleano o error HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
