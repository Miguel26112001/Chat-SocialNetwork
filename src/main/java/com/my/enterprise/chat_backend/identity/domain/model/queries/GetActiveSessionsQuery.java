package com.my.enterprise.chat_backend.identity.domain.model.queries;

import java.util.UUID;

/**
 * Consulta CQRS del lado READ: sesiones vigentes (no revocadas y no
 * expiradas) de un usuario. Alimenta el caso de uso "mis dispositivos".
 * El instante de corte lo aporta el reloj de la aplicación, no la consulta.
 */
public record GetActiveSessionsQuery(UUID userId) {
}
