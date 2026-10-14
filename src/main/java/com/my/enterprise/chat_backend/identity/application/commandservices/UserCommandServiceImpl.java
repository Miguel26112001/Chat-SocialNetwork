package com.my.enterprise.chat_backend.identity.application.commandservices;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.PasswordHasher;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.UserRepository;
import com.my.enterprise.chat_backend.identity.domain.exceptions.EmailAlreadyExistsException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UserNotFoundException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UsernameAlreadyExistsException;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ActivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ChangePasswordCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.DeactivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RegisterUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.PasswordHash;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import com.my.enterprise.chat_backend.identity.domain.services.PasswordPolicy;
import com.my.enterprise.chat_backend.identity.domain.services.UserCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UserCommandService} (agregado User).
 *
 * Orquestación de las escrituras sobre la cuenta:
 *  - unicidad de username/email contra el repositorio (invariante de
 *    contexto, no del agregado);
 *  - política de contraseñas + hash delegado al puerto PasswordHasher;
 *  - una cuenta desactivada no puede operar (ensureCanAuthenticate);
 *  - los eventos de los agregados los publica automáticamente Spring
 *    Data al llamar a save(); los consumidores deben usar
 *    {@code @TransactionalEventListener(phase = AFTER_COMMIT)}.
 *
 * Como servicio de aplicación, orquesta también al agregado
 * RefreshToken cuando una operación lo requiere (p. ej. al
 * desactivar una cuenta se revocan todas sus sesiones).
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final Clock clock;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  PasswordHasher passwordHasher,
                                  PasswordPolicy passwordPolicy,
                                  Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.clock = clock;
    }

    /**
     * Registra una nueva cuenta.
     * Orquestación: construir VOs (validan formato) → comprobar
     * unicidad → validar política de contraseña → delegar hash al
     * puerto → crear el agregado (publica UserRegisteredEvent) → guardar.
     */
    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        Username username = new Username(command.username());
        Email email = new Email(command.email());

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(command.email());
        }

        passwordPolicy.validate(command.rawPassword());
        PasswordHash passwordHash = passwordHasher.hash(command.rawPassword());

        User user = User.register(username, email, passwordHash, clock.now());
        userRepository.save(user);

        return user;
    }

    /**
     * Cambia la contraseña. Requiere la contraseña ACTUAL: se verifica
     * antes de permitir el cambio. El agregado publica
     * PasswordChangedEvent (un consumidor puede revocar las demás
     * sesiones reaccionando a ese evento).
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        user.ensureCanAuthenticate();

        if (!passwordHasher.matches(command.currentRawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        passwordPolicy.validate(command.newRawPassword());
        user.changePassword(passwordHasher.hash(command.newRawPassword()), clock.now());
        userRepository.save(user);
    }

    /**
     * Desactiva la cuenta (soft delete de users.is_active) y revoca
     * TODAS sus sesiones: una cuenta desactivada no debe seguir
     * emitiendo tokens nuevos.
     *
     * El agregado User publica UserDeactivatedEvent; Social Graph y
     * Engagement reaccionarán para suspender su actividad.
     */
    @Override
    @Transactional
    public void deactivate(DeactivateUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.deactivate(clock.now());
        userRepository.save(user);

        refreshTokenRepository.findActiveByUserId(command.userId(), clock.now())
                .forEach(token -> {
                    token.revoke(clock.now());
                    refreshTokenRepository.save(token);
                });
    }

    /** Reactiva una cuenta desactivada. */
    @Override
    @Transactional
    public void activate(ActivateUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.activate(clock.now());
        userRepository.save(user);
    }
}
