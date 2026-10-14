package com.my.enterprise.chat_backend.identity.domain.services;

import com.my.enterprise.chat_backend.identity.domain.model.commands.ActivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ChangePasswordCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.DeactivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RegisterUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.dto.UserDto;

/**
 * Contrato de los comandos (lado WRITE, CQRS) del agregado User.
 * <p>
 * Interfaz en la capa de dominio: define QUÉ puede hacerse con una
 * cuenta, sin CÓMO. La implementación vive en la capa de aplicación
 * (application.commandservices) y orquesta puertos outbound.
 * <p>
 * Solo operaciones sobre el agregado User. El ciclo de vida de las
 * sesiones (login/logout/renovación) pertenece a
 * {@link RefreshTokenCommandService}.
 */
public interface UserCommandService {

    /** Registra una nueva cuenta. Devuelve su proyección (nunca el agregado). */
    UserDto register(RegisterUserCommand command);

    /** Cambia la contraseña (requiere la contraseña actual). */
    void changePassword(ChangePasswordCommand command);

    /** Desactiva la cuenta (soft delete de users.is_active). */
    void deactivate(DeactivateUserCommand command);

    /** Reactiva una cuenta desactivada. */
    void activate(ActivateUserCommand command);
}
