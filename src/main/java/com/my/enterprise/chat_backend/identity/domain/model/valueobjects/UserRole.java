package com.my.enterprise.chat_backend.identity.domain.model.valueobjects;

/**
 * Rol de una cuenta dentro del sistema.
 *
 * USER: cuenta común. Solo puede operar sobre sí misma.
 * ADMIN: administración. Podrá operar sobre cualquier cuenta
 * (p. ej. desactivar/reactivar usuarios, futuras funciones).
 *
 * Se persiste como VARCHAR (USER/ADMIN) para que la base de datos
 * sea legible sin tablas de lookup.
 */
public enum UserRole {

    USER,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
