package com.my.enterprise.chat_backend.identity.application.queryservices;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.domain.model.dto.SessionDto;
import com.my.enterprise.chat_backend.identity.domain.model.queries.GetActiveSessionsQuery;
import com.my.enterprise.chat_backend.identity.domain.services.RefreshTokenQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link RefreshTokenQueryService}
 * (agregado RefreshToken).
 *
 * Alimenta el caso de uso "mis dispositivos / sesiones activas".
 */
@Service
public class RefreshTokenQueryServiceImpl implements RefreshTokenQueryService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshTokenQueryServiceImpl(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    /** Sesiones vigentes (no revocadas y no expiradas) de un usuario. */
    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> findActiveSessions(GetActiveSessionsQuery query) {
        return refreshTokenRepository.findActiveByUserId(query.userId(), clock.now()).stream()
                .map(SessionDto::from)
                .toList();
    }
}
