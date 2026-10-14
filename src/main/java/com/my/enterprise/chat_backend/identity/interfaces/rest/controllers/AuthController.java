package com.my.enterprise.chat_backend.identity.interfaces.rest.controllers;

import com.my.enterprise.chat_backend.identity.domain.model.commands.LoginCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.LogoutCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RefreshSessionCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.RegisterUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetActiveSessionsQuery;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenCommandService;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenQueryService;
import com.my.enterprise.chat_backend.identity.domain.services.UserCommandService;
import com.my.enterprise.chat_backend.identity.interfaces.rest.UserPrincipal;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.AuthTokensResource;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.LoginRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.LogoutRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.RefreshRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.RegisterRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.SessionResource;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.UserResource;
import com.my.enterprise.chat_backend.identity.interfaces.rest.transform.AuthTokensTransform;
import com.my.enterprise.chat_backend.identity.interfaces.rest.transform.SessionTransform;
import com.my.enterprise.chat_backend.identity.interfaces.rest.transform.UserTransform;
import com.my.enterprise.chat_backend.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Auth", description = "Registro, inicio de sesión y gestión de sesiones (Identity)")
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

    @Operation(summary = "Registrar una cuenta",
            description = "Crea un usuario nuevo. No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username o email ya registrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResource> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserTransform.toResource(
                userCommandService.register(new RegisterUserCommand(
                        request.username(), request.email(), request.password()))));
    }

    @Operation(summary = "Iniciar sesión",
            description = "Autentica con username o email y devuelve el par de tokens "
                    + "(access JWT de corta duración + refresh token). No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada, par de tokens emitido",
                    content = @Content(schema = @Schema(implementation = AuthTokensResource.class))),
            @ApiResponse(responseCode = "400", description = "Petición inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokensResource> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthTokensTransform.toResource(
                refreshTokenCommandService.login(new LoginCommand(
                        request.usernameOrEmail(), request.password()))));
    }

    @Operation(summary = "Cerrar sesión en este dispositivo",
            description = "Revoca el refresh token indicado. Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        UUID userId = UserPrincipal.current();
        refreshTokenCommandService.logout(new LogoutCommand(userId, request.refreshToken()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Renovar la sesión",
            description = "Intercambia un refresh token vigente por un par nuevo (rotación: "
                    + "el token usado queda revocado). No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Par de tokens nuevo emitido",
                    content = @Content(schema = @Schema(implementation = AuthTokensResource.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado o revocado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResource> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(AuthTokensTransform.toResource(
                refreshTokenCommandService.refreshSession(new RefreshSessionCommand(
                        request.refreshToken()))));
    }

    @Operation(summary = "Listar mis sesiones activas",
            description = "Devuelve los refresh tokens vigentes del usuario autenticado "
                    + "('mis dispositivos'). Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de sesiones activas",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SessionResource.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResource>> sessions() {
        UUID userId = UserPrincipal.current();
        return ResponseEntity.ok(SessionTransform.toResourceList(
                refreshTokenQueryService.findActiveSessions(new GetActiveSessionsQuery(userId))));
    }
}
