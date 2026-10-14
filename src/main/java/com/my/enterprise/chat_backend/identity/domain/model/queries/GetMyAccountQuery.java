package com.my.enterprise.chat_backend.identity.domain.model.queries;

import java.util.UUID;

/**
 * Consulta CQRS del lado READ: obtener la cuenta propia del usuario
 * autenticado. Difiere de {@link GetUserByIdQuery} en la intención:
 * esta query expresa "dame MI cuenta" (el id viene del token, no de
 * un parámetro), mientras que aquella expresa "dame LA cuenta X".
 */
public record GetMyAccountQuery(UUID userId) {
}
