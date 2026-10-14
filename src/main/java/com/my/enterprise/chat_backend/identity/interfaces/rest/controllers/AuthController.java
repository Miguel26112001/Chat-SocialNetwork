package com.my.enterprise.chat_backend.identity.interfaces.rest.controllers;

import com.my.enterprise.chat_backend.identity.application.dto.SessionDto;
import com.my.enterprise.chat_backend.identity.application.dto.UserDto;
import com.my.enterprise.chat_backend.identity.domain.model.AuthTokens;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LoginCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LogoutCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RefreshSessionCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RegisterUserCommand;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenCommandService;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenQueryService;
import com.my.enterprise.chat_backend.identity.domain.services.UserCommandService;
import com.my.enterprise.chat_backend.identity.interfaces.rest.UserPrincipal;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.LoginRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.LogoutRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.RefreshRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST API de autenticación del contexto Identity.
 *
 *  - POST /register  → crea cuenta (201)
 *  - POST /login     → par de tokens (200)
 *  - POST /logout    → revoca un refresh token (204)
 *  - POST /refresh   → rota la sesión (200)
 *  - GET  /sessions  → sesiones activas del usuario autenticado (200)
 *
 * register/login/refresh son públicas (ver SecurityConfiguration);
 * el resto requiere JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserCommandService userCommandService;
    private final RefreshTokenCommandService refreshTokenCommandService;
    private final RefreshTokenQueryService refreshTokenQueryService;

    public AuthController(UserCommandService userCommandService,
                          RefreshTokenCommandService refreshTokenCommandService,
                          RefreshTokenQueryService refreshTokenQueryService) {
        this.userCommandService = userCommandService;
        this.refreshTokenCommandService = refreshTokenCommandService;
        this.refreshTokenQueryService = refreshTokenQueryService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        User user = userCommandService.register(new RegisterUserCommand(
                request.username(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
    }

    @PostMapping("/login")
    public AuthTokens login(@Valid @RequestBody LoginRequest request) {
        return refreshTokenCommandService.login(new LoginCommand(
                request.usernameOrEmail(), request.password()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        UUID userId = UserPrincipal.current();
        refreshTokenCommandService.logout(new LogoutCommand(userId, request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public AuthTokens refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshTokenCommandService.refreshSession(new RefreshSessionCommand(
                request.refreshToken()));
    }

    @GetMapping("/sessions")
    public List<SessionDto> sessions() {
        UUID userId = UserPrincipal.current();
        return refreshTokenQueryService.findActiveSessions(userId, Instant.now()).stream()
                .map(SessionDto::from)
                .toList();
    }
}
