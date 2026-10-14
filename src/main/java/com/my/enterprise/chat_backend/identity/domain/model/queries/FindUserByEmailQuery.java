package com.my.enterprise.chat_backend.identity.domain.model.queries;

/** Consulta CQRS del lado READ: obtener una cuenta por correo exacto (normalizado a minúsculas). */
public record FindUserByEmailQuery(String email) {
}
