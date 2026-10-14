package com.my.enterprise.chat_backend.identity.interfaces.rest.transform;

import com.my.enterprise.chat_backend.identity.domain.model.dto.UserDto;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.UserResource;

/**
 * Transforma el DTO de dominio (lo que entrega la capa de aplicación)
 * al recurso de la API (lo que serializa la capa de interfaces).
 * Funciones estáticas puras, sin estado ni dependencias.
 */
public final class UserTransform {

    private UserTransform() {
    }

    public static UserResource toResource(UserDto dto) {
        return new UserResource(
                dto.id(),
                dto.username(),
                dto.email(),
                dto.active(),
                dto.createdAt(),
                dto.updatedAt());
    }
}
