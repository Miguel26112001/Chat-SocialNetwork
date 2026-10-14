package com.my.enterprise.chat_backend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de OpenAPI / Swagger UI.
 *
 * - El esquema {@code bearerAuth} habilita el botón "Authorize" en
 *   Swagger UI: pega el access token JWT y se envía como
 *   {@code Authorization: Bearer <token>} en los endpoints protegidos.
 * - Cada controller añade su {@code @Tag} y cada operación su
 *   {@code @Operation} + {@code @ApiResponse}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Chat Social API",
                version = "v1",
                description = "API del monolito modular Chat Social. "
                        + "Empieza por POST /api/v1/auth/register y POST /api/v1/auth/login; "
                        + "luego pulsa 'Authorize' con el access token para probar los endpoints protegidos.",
                contact = @Contact(name = "Chat Social Backend")),
        servers = @Server(url = "/", description = "Servidor actual"))
@SecurityScheme(
        name = OpenApiConfiguration.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Access token JWT obtenido en POST /api/v1/auth/login. "
                + "Formato en Swagger UI: pega solo el token (sin 'Bearer ').")
public class OpenApiConfiguration {

    /** Nombre del esquema de seguridad JWT referenciado con @SecurityRequirement. */
    public static final String BEARER_AUTH = "bearerAuth";
}
