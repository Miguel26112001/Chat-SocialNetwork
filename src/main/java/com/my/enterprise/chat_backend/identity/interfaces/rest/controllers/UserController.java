package com.my.enterprise.chat_backend.identity.interfaces.rest.controllers;

import com.my.enterprise.chat_backend.identity.application.dto.UserDto;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ActivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.ChangePasswordCommand;
import com.my.enterprise.chat_backend.identity.domain.model.commands.DeactivateUserCommand;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByIdQuery;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetUserByUsernameQuery;
import com.my.enterprise.chat_backend.identity.domain.services.UserCommandService;
import com.my.enterprise.chat_backend.identity.domain.services.UserQueryService;
import com.my.enterprise.chat_backend.identity.interfaces.rest.UserPrincipal;
import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ChangePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST API de cuentas del contexto Identity.
 * Todos los endpoints requieren autenticación JWT.
 */
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

    /** El perfil del propio usuario autenticado. */
    @GetMapping("/me")
    public UserDto me() {
        UUID userId = UserPrincipal.current();
        return UserDto.from(userQueryService.getUserById(new GetUserByIdQuery(userId)));
    }

    /** Perfil público de un usuario por id. */
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return UserDto.from(userQueryService.getUserById(new GetUserByIdQuery(id)));
    }

    /** Perfil público de un usuario por username exacto. */
    @GetMapping("/username/{username}")
    public UserDto getByUsername(@PathVariable String username) {
        return UserDto.from(userQueryService.getUserByUsername(new GetUserByUsernameQuery(username)));
    }

    /** Cambio de contraseña del propio usuario. */
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = UserPrincipal.current();
        userCommandService.changePassword(new ChangePasswordCommand(
                userId, request.currentPassword(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    /** Desactiva una cuenta (soft delete). */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userCommandService.deactivate(new DeactivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }

    /** Reactiva una cuenta desactivada. */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        userCommandService.activate(new ActivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }
}
