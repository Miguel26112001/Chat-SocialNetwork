package com.my.enterprise.chat_backend.identity.infrastructure.persistence.jpa.repositories;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.RefreshTokenRepository;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del puerto {@code RefreshTokenRepository} sobre Spring Data JPA.
 */
@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public JpaRefreshTokenRepository(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId, Instant now) {
        return jpaRepository.findActiveByUserId(userId, now);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpaRepository.save(token);
    }
}
