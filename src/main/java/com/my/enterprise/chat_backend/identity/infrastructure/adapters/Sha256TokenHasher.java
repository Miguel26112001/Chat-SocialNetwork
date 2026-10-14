package com.my.enterprise.chat_backend.identity.infrastructure.adapters;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.TokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Adaptador del puerto {@code TokenHasher}: SHA-256.
 *
 * El refresh token ya tiene 256 bits de entropía propia, así que no
 * requiere salt: el hash es determinista y permite buscar el token
 * por su hash en la tabla identity.refresh_tokens.
 */
@Component
public class Sha256TokenHasher implements TokenHasher {

    @Override
    public String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
