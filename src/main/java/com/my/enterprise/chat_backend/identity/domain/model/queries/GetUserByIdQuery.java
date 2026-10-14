package com.my.enterprise.chat_backend.identity.domain.model.queries;

import java.util.UUID;

/** Consulta CQRS del lado READ: obtener una cuenta por id. */
public record GetUserByIdQuery(UUID userId) {
}
