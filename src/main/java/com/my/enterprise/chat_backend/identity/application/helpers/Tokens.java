package com.my.enterprise.chat_backend.identity.application.helpers;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generador de tokens criptográficamente seguros (JDK puro).
 *
 * 32 bytes de entropía → Base64 URL sin padding (~43 caracteres).
 * Se usa para el refresh token en texto plano; de él solo se
 * persistirá el hash.
 */
public final class Tokens {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private Tokens() {
    }

    public static String newRawRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}
