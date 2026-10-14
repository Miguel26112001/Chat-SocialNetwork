package com.my.enterprise.chat_backend.identity.application.commandservices;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.PasswordHasher;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenHasher;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenService;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.UserRepository;
import com.my.enterprise.chat_backend.identity.domain.exceptions.InvalidCredentialsException;
import com.my.enterprise.chat_backend.identity.domain.exceptions.UserNotFoundException;
import com.my.enterprise.chat_backend.identity.domain.model.AuthTokens;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LoginCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LogoutCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RefreshSessionCommand;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación de {@link RefreshTokenCommandService}
 * (agregado RefreshToken).
 *
 * El ciclo de vida de las sesiones: login emite, logout revoca y
 * refreshSession rota.
 *
 * Reglas transversales:
 *  - los errores de autenticación nunca distinguen "usuario
 *    inexistente" de "contraseña incorrecta" (anti user-enumeration);
 *  - una cuenta desactivada no puede operar (ensureCanAuthenticate);
 *  - un token robado queda inutilizado al usarse (rotación).
 */
@Service
public class RefreshTokenCommandServiceImpl implements RefreshTokenCommandService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenHasher tokenHasher;
    private final Clock clock;
    private final TokenPairIssuer tokenPairIssuer;

    public RefreshTokenCommandServiceImpl(UserRepository userRepository,
                                          RefreshTokenRepository refreshTokenRepository,
                                          PasswordHasher passwordHasher,
                                          TokenHasher tokenHasher,
                                          Clock clock,
                                          TokenService tokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
        this.tokenPairIssuer = new TokenPairIssuer(tokenService, tokenHasher, refreshTokenRepository, clock);
    }

    /**
     * Inicia sesión con username o email.
     * Orquestación: localizar al usuario → descartar cuentas
     * desactivadas ANTES de comparar la contraseña → verificar la
     * contraseña con el puerto → emitir el par de tokens.
     */
    @Override
    @Transactional
    public AuthTokens login(LoginCommand command) {
        User user = findByIdentifier(command.usernameOrEmail());
        user.ensureCanAuthenticate();
        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokenPairIssuer.issue(user);
    }

    /**
     * Cierra sesión en un dispositivo: revoca el refresh token recibido.
     * Idempotente: si el token ya no existe, no hace nada.
     */
    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        String tokenHash = tokenHasher.hash(command.rawRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.revoke(clock.now());
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Renueva la sesión: intercambia un refresh token vigente por un
     * par nuevo (rotación).
     */
    @Override
    @Transactional
    public AuthTokens refreshSession(RefreshSessionCommand command) {
        String tokenHash = tokenHasher.hash(command.rawRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidCredentialsException::new);

        if (token.isRevoked() || token.isExpired(clock.now())) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException(token.getUserId()));
        user.ensureCanAuthenticate();

        token.revoke(clock.now());
        refreshTokenRepository.save(token);

        return tokenPairIssuer.issue(user);
    }

    /**
     * Un identificador con "@" se trata como email; sin "@", como
     * username. Así un username con formato inválido no enmascara
     * el error de "credenciales inválidas".
     */
    private User findByIdentifier(String identifier) {
        Optional<User> candidate;
        if (identifier != null && identifier.contains("@")) {
            candidate = userRepository.findByEmail(new Email(identifier));
        } else {
            candidate = userRepository.findByUsername(new Username(identifier));
        }
        return candidate.orElseThrow(InvalidCredentialsException::new);
    }
}
