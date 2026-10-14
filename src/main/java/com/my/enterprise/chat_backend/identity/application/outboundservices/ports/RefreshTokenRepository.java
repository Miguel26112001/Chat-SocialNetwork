package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia del agregado RefreshToken.
 */
public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Sesiones vigentes (no revocadas y no expiradas) de un usuario. */
    List<RefreshToken> findActiveByUserId(UUID userId, Instant now);

    RefreshToken save(RefreshToken token);
}
