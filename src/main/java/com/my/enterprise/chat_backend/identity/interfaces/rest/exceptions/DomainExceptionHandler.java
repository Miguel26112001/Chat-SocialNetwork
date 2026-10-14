package com.my.enterprise.chat_backend.identity.interfaces.rest.exceptions;

import com.my.enterprise.chat_backend.identity.domain.exceptions.DomainException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.EmailAlreadyExistsException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.ForbiddenException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InactiveUserException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidEmailException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidPasswordException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidUsernameException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UserNotFoundException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UsernameAlreadyExistsException;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce excepciones de dominio a respuestas HTTP con el formato
 * {@link ErrorResponse}. Cada excepción de dominio tiene un código
 * semántico; cualquier otra excepción se traduce a 500 genérico (sin
 * filtrar detalles internos al cliente).
 */
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponse> inactiveUser(
            InactiveUserException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(
            ForbiddenException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> usernameAlreadyExists(
            UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> emailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public ResponseEntity<ErrorResponse> invalidUsername(
            InvalidUsernameException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> invalidEmail(
            InvalidEmailException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> invalidPassword(
            InvalidPasswordException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /** Validación de transporte (@Valid en resources): 400 con el primer error de campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidArgument(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Petición inválida";
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    /** Cualquier otra excepción de dominio no mapeada: 400 genérico. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> domainException(
            DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /**
     * Violación de un constraint de BD (p. ej. dos registros simultáneos
     * que superan el chequeo previo de unicidad y chocan en el UNIQUE).
     * Se traduce a 409 con el mismo mensaje que el caso secuencial, para
     * que el cliente no distinga la condición de carrera (ni vea un 500).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String constraint = findConstraintName(ex);
        String message;
        if (constraint != null && constraint.contains("username")) {
            message = "Ya existe un usuario con ese nombre";
        } else if (constraint != null && constraint.contains("email")) {
            message = "Ya existe una cuenta con ese correo";
        } else {
            message = "La operación viola una restricción de unicidad";
        }
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** Excepciones desconocidas: 500 sin detalles internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandled(
            HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Error interno del servidor",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, RuntimeException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Extrae el nombre del constraint violado recorriendo la cadena de
     * causas hasta el {@code ConstraintViolationException} de Hibernate.
     * Devuelve {@code null} si no se puede determinar.
     */
    private String findConstraintName(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null) {
                return violation.getConstraintName();
            }
            current = current.getCause();
        }
        return null;
    }
}
