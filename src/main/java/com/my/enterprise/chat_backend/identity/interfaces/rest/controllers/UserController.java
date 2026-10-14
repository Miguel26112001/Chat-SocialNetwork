package com.my.enterprise.chat_backend.identity.interfaces.rest.controllers;

import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.UserResource;
import com.my.enterprise.chat_backend.identity.interfaces.rest.transform.UserTransform;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ActivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ChangePasswordCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.DeactivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.queries.FindUserByEmailQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetMyAccountQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByIdQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByUsernameQuery;
import com.my.enterprise.chat_backend.identity.domain.services.UserCommandService;
import com.my.enterprise.chat_backend.identity.domain.services.UserQueryService;
import com.my.enterprise.chat_backend.identity.interfaces.rest.UserPrincipal;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ChangePasswordRequest;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse;
import com.my.enterprise.chat_backend.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "Consulta y gestión de cuentas (Identity). Todos los endpoints requieren JWT.")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UserController(UserQueryService userQueryService,
                          UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    @Operation(summary = "Obtener mi cuenta", description = "Devuelve la cuenta del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResource> me() {
        UUID userId = UserPrincipal.current();
        return ResponseEntity.ok(UserTransform.toResource(
                userQueryService.getMyAccount(new GetMyAccountQuery(userId))));
    }

    @Operation(summary = "Obtener una cuenta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserTransform.toResource(
                userQueryService.getUserById(new GetUserByIdQuery(id))));
    }

    @Operation(summary = "Obtener una cuenta por username exacto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResource> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(UserTransform.toResource(
                userQueryService.getUserByUsername(new GetUserByUsernameQuery(username))));
    }

    @Operation(summary = "Obtener una cuenta por correo exacto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Correo con formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResource> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(UserTransform.toResource(
                userQueryService.findUserByEmail(new FindUserByEmailQuery(email))));
    }

    @Operation(summary = "Cambiar mi contraseña",
            description = "Requiere la contraseña actual. Cambiarla no revoca las demás sesiones "
                    + "(las revoca el consumidor del evento PasswordChangedEvent cuando exista).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña cambiada"),
            @ApiResponse(responseCode = "400", description = "Nueva contraseña inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o contraseña actual incorrecta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = UserPrincipal.current();
        userCommandService.changePassword(new ChangePasswordCommand(
                userId, request.currentPassword(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar una cuenta",
            description = "Soft delete (is_active = false) y revocación de todas sus sesiones.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta desactivada"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userCommandService.deactivate(new DeactivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivar una cuenta desactivada")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta reactivada"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        userCommandService.activate(new ActivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }
}
