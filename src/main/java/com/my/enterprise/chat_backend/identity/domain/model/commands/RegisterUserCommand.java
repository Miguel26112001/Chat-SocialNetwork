package com.my.enterprise.chat_backend.identity.domain.model.commands;

/**
 * Intención de registrar una nueva cuenta.
 * Command del lado WRITE (CQRS): lo ejecuta un CommandService.
 * Lleva datos crudos de transporte; la validación de formato y la
 * construcción de value objects ocurren al ejecutarlo.
 */
public record RegisterUserCommand(
        String username,
        String email,
        String rawPassword) {
}
