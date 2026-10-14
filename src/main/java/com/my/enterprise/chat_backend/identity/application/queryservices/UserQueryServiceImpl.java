package com.my.enterprise.chat_backend.identity.application.queryservices;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.UserRepository;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UserNotFoundException;
import com.my.enterprise.chat_backend.identity.domain.model.dto.UserDto;
import com.my.enterprise.chat_backend.identity.domain.model.queries.FindUserByEmailQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetMyAccountQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByIdQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByUsernameQuery;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import com.my.enterprise.chat_backend.identity.domain.services.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UserQueryService} (agregado User).
 * <p>
 * Devuelve agregados de dominio; la capa de interfaces los transforma
 * a DTOs/resources. Solo lectura: no muta estado ni publica eventos.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Obtiene una cuenta por id. */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(GetUserByIdQuery query) {
        return UserDto.from(userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId())));
    }

    /** Obtiene una cuenta por username exacto. */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(GetUserByUsernameQuery query) {
        return UserDto.from(userRepository.findByUsername(new Username(query.username()))
                .orElseThrow(() -> new UserNotFoundException(query.username())));
    }

    /** Obtiene una cuenta por correo exacto (el VO Email ya normaliza a minúsculas). */
    @Override
    @Transactional(readOnly = true)
    public UserDto findUserByEmail(FindUserByEmailQuery query) {
        return UserDto.from(userRepository.findByEmail(new Email(query.email()))
                .orElseThrow(() -> new UserNotFoundException(query.email())));
    }

    /** Obtiene la cuenta propia del usuario autenticado. */
    @Override
    @Transactional(readOnly = true)
    public UserDto getMyAccount(GetMyAccountQuery query) {
        return UserDto.from(userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId())));
    }
}
