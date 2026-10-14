package com.my.enterprise.chat_backend.identity.domain.model.aggregates;

import com.my.enterprise.chat_backend.identity.domain.exceptions.AccountLockedException;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.PasswordHash;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bloqueo temporal por intentos fallidos (puro dominio, sin Spring).
 */
class UserLockoutTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private static User newUser() {
        return User.register(
                new Username("ana_dev"),
                new Email("ana@example.com"),
                new PasswordHash("$2a$10$hashdeprueba0123456789012345678901234567890123456"),
                Instant.now());
    }

    @Test
    void belowThreshold_doesNotLock() {
        User user = newUser();
        Instant now = Instant.now();

        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            user.recordFailedLogin(now, MAX_ATTEMPTS, LOCK_DURATION);
        }

        assertDoesNotThrow(() -> user.ensureCanAuthenticate(now));
    }

    @Test
    void atThreshold_locksAccount() {
        User user = newUser();
        Instant now = Instant.now();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            user.recordFailedLogin(now, MAX_ATTEMPTS, LOCK_DURATION);
        }

        assertThrows(AccountLockedException.class, () -> user.ensureCanAuthenticate(now));
    }

    @Test
    void afterLockExpires_authenticatesAgain() {
        User user = newUser();
        Instant now = Instant.now();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            user.recordFailedLogin(now, MAX_ATTEMPTS, LOCK_DURATION);
        }

        assertDoesNotThrow(() -> user.ensureCanAuthenticate(now.plus(LOCK_DURATION).plusSeconds(1)));
    }

    @Test
    void successfulLogin_resetsCounter() {
        User user = newUser();
        Instant now = Instant.now();

        user.recordFailedLogin(now, MAX_ATTEMPTS, LOCK_DURATION);
        assertTrue(user.resetFailedLogins(now));
        assertFalse(user.resetFailedLogins(now));
        assertDoesNotThrow(() -> user.ensureCanAuthenticate(now));
    }
}
