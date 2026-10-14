package com.my.enterprise.chat_backend.identity.interfaces.rest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Acceso al usuario autenticado en el SecurityContext.
 * El principal es el id de usuario (UUID), tal como lo deja
 * JwtAuthenticationFilter.
 */
public final class UserPrincipal {

    private UserPrincipal() {
    }

    /** Id del usuario autenticado. */
    public static UUID current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad");
        }
        return userId;
    }
}
