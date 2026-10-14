package com.my.enterprise.chat_backend.identity.application.queryservices;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de {@link RefreshTokenQueryService}
 * (agregado RefreshToken).
 *
 * Alimenta el caso de uso "mis dispositivos / sesiones activas".
 */
@Service
public class RefreshTokenQueryServiceImpl implements RefreshTokenQueryService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenQueryServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /** Sesiones vigentes (no revocadas y no expiradas) de un usuario. */
    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findActiveSessions(UUID userId, Instant now) {
        return refreshTokenRepository.findActiveByUserId(userId, now);
    }
}
