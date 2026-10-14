package com.my.enterprise.chat_backend.identity.interfaces.rest.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Traductor condición-de-carrera → 409.
 *
 * Cuando dos registros simultáneos superan el chequeo previo de unicidad,
 * PostgreSQL rechaza al segundo con un UNIQUE violation. Estos tests
 * verifican que esa excepción de infraestructura se traduzca al mismo
 * 409 semántico del caso secuencial (y nunca a un 500).
 */
class DomainExceptionHandlerTest {

    private final DomainExceptionHandler handler = new DomainExceptionHandler();
    private final HttpServletRequest request = stubRequest("/api/v1/auth/register");

    @Test
    void duplicateUsernameConstraint_mapsTo409() {
        DataIntegrityViolationException ex = violation("uq_identity_users_username");

        ResponseEntity<?> response = handler.dataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ya existe un usuario con ese nombre", body(response).message());
    }

    @Test
    void duplicateEmailConstraint_mapsTo409() {
        DataIntegrityViolationException ex = violation("uq_users_email");

        ResponseEntity<?> response = handler.dataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ya existe una cuenta con ese correo", body(response).message());
    }

    @Test
    void unknownConstraint_mapsToGeneric409() {
        DataIntegrityViolationException ex = violation("uq_algo_desconocido");

        ResponseEntity<?> response = handler.dataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("La operación viola una restricción de unicidad", body(response).message());
    }

    @Test
    void violationWithoutConstraintName_mapsToGeneric409() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("integrity error", new RuntimeException("root"));

        ResponseEntity<?> response = handler.dataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    /** Reconstruye la cadena real: Spring envuelve el ConstraintViolationException de Hibernate. */
    private static DataIntegrityViolationException violation(String constraintName) {
        SQLException sql = new SQLException(
                "ERROR: duplicate key value violates unique constraint \"" + constraintName + "\"",
                "23505");
        ConstraintViolationException hibernate =
                new ConstraintViolationException("could not execute statement", sql, constraintName);
        return new DataIntegrityViolationException("integrity error", hibernate);
    }

    private static com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse body(
            ResponseEntity<?> response) {
        assertNotNull(response.getBody());
        return (com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse) response.getBody();
    }

    /** Stub mínimo de HttpServletRequest (solo se usa getRequestURI). */
    private static HttpServletRequest stubRequest(String uri) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                DomainExceptionHandlerTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getRequestURI")) {
                        return uri;
                    }
                    return null;
                });
    }
}
