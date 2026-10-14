package com.my.enterprise.chat_backend.identity.domain.services;

import com.my.enterprise.chat_backend.identity.domain.model.dto.SessionDto;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetActiveSessionsQuery;

import java.util.List;

/**
 * Contrato de las consultas (lado READ, CQRS) del agregado RefreshToken.
 * <p>
 * Interfaz en la capa de dominio; la implementación vive en la capa
 * de aplicación (application.queryservices).
 */
public interface RefreshTokenQueryService {

    /**
     * Sesiones vigentes (no revocadas y no expiradas) de un usuario.
     * Alimenta el caso de uso "mis dispositivos / sesiones activas".
     */
    List<SessionDto> findActiveSessions(GetActiveSessionsQuery query);
}
