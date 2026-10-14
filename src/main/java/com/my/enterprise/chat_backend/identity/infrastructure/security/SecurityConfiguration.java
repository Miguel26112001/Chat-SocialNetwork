package com.my.enterprise.chat_backend.identity.infrastructure.security;

import com.my.enterprise.chat_backend.identity.interfaces.rest.resources.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de seguridad del contexto Identity.
 *
 * - Sesión sin estado (stateless): toda la información de
 *   autenticación viaja en el JWT.
 * - CSRF desactivado: no hay cookies de sesión.
 * - Rutas públicas: registro, login y renovación de sesión (el cliente
 *   aún no tiene tokens válidos).
 * - El resto requieren autenticación JWT.
 *
 * Nota de arquitectura: vive en identity (no en shared) porque usa
 * el puerto TokenService del propio contexto; shared no debe depender
 * de los contextos.
 */
@Component
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @org.springframework.context.annotation.Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(res, req, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized", "No autenticado o token inválido"))
                        .accessDeniedHandler((req, res, e) ->
                                writeError(res, req, HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden", "No tienes permisos para este recurso")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            int status,
                            String error,
                            String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = ErrorResponse.of(status, error, message, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
