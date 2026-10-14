package com.my.enterprise.chat_backend.identity.domain.exceptions;

import lombok.Getter;

import java.time.Instant;

/**
 * La cuenta está bloqueada temporalmente por exceso de intentos
 * fallidos de login. Se traduce a HTTP 423 (Locked).
 */
@Getter
public class AccountLockedException extends DomainException {

    private final Instant lockedUntil;

    public AccountLockedException(Instant lockedUntil) {
        super("Cuenta bloqueada temporalmente. Inténtalo de nuevo más tarde");
        this.lockedUntil = lockedUntil;
    }

}
