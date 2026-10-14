package com.my.enterprise.chat_backend.identity.domain.services;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Servicio de dominio: política de bloqueo por intentos fallidos.
 *
 * Reglas actuales:
 *  - 5 fallos consecutivos bloquean la cuenta;
 *  - el bloqueo dura 15 minutos;
 *  - un login exitoso reinicia el contador.
 *
 * Es puro salvo el reloj que recibe por parámetro: no toca puertos
 * ni infraestructura.
 */
@Component
public class AccountLockoutPolicy {

    public static final int MAX_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    public int maxAttempts() {
        return MAX_ATTEMPTS;
    }

    public Duration lockDuration() {
        return LOCK_DURATION;
    }
}
