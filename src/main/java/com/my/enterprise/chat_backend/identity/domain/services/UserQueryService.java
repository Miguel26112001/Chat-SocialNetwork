package com.my.enterprise.chat_backend.identity.domain.services;

import com.my.enterprise.chat_backend.identity.domain.model.dto.UserDto;
import com.my.enterprise.chat_backend.identity.domain.model.queries.FindUserByEmailQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetMyAccountQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByIdQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByUsernameQuery;

/**
 * Contrato de las consultas (lado READ, CQRS) del agregado User.
 * <p>
 * Interfaz en la capa de dominio. La implementación vive en la capa
 * de aplicación (application.queryservices) y devuelve agregados de
 * dominio; la capa de interfaces los transforma a DTOs/resources.
 * <p>
 * Solo lectura: no muta estado ni publica eventos.
 */
public interface UserQueryService {

    /** Obtiene una cuenta por id. */
    UserDto getUserById(GetUserByIdQuery query);

    /** Obtiene una cuenta por username exacto. */
    UserDto getUserByUsername(GetUserByUsernameQuery query);

    /** Obtiene una cuenta por correo exacto (normalizado a minúsculas). */
    UserDto findUserByEmail(FindUserByEmailQuery query);

    /** Obtiene la cuenta propia del usuario autenticado. */
    UserDto getMyAccount(GetMyAccountQuery query);
}
