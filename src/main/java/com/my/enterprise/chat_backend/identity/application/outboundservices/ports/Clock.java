package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import java.time.Instant;

/**
 * Puerto de reloj: abstrae el tiempo del sistema.
 * <p>
 * Permite tests deterministas (reloj fijo) y evita esparcir
 * {@code Instant.now()} por la capa de aplicación.
 */
public interface Clock {

    Instant now();
}
