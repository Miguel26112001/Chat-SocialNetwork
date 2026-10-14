package com.my.enterprise.chat_backend.identity.domain.model.queries;

/** Consulta CQRS del lado READ: obtener una cuenta por username exacto. */
public record GetUserByUsernameQuery(String username) {
}
