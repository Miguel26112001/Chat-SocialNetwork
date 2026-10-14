package com.my.enterprise.chat_backend.identity.interfaces.rest.transform;

import com.my.enterprise.chat_backend.identity.domain.model.dto.SessionDto;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.SessionResource;

import java.util.List;

/**
 * Transforma el DTO de dominio (lo que entrega la capa de aplicación)
 * al recurso de la API (lo que serializa la capa de interfaces).
 * Funciones estáticas puras, sin estado ni dependencias.
 */
public final class SessionTransform {

    private SessionTransform() {
    }

    public static SessionResource toResource(SessionDto dto) {
        return new SessionResource(dto.id(), dto.createdAt(), dto.expiresAt());
    }

    public static List<SessionResource> toResourceList(List<SessionDto> dtos) {
        return dtos.stream()
                .map(SessionTransform::toResource)
                .toList();
    }
}
