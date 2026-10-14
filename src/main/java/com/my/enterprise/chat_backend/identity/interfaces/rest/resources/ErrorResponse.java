package com.my.enterprise.chat_backend.identity.interfaces.rest.resources;

import java.time.Instant;

/**
 * Cuerpo estándar de error de la API.
 * Formato consistente para todos los fallos (RFC 7807 simplificado,
 * sin depender de librerías externas).
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
