package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

/**
 * Puerto de hashing de refresh tokens.
 * <p>
 * REGLA DE ORO: solo se guarda el hash del token, nunca el token en
 * texto plano. Un refresh token tiene entropía propia (256 bits),
 * por lo que no requiere salt.
 */
public interface TokenHasher {

    String hash(String rawToken);
}
