package com.my.enterprise.chat_backend.identity.infrastructure.persistence.jpa.repositories;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA del agregado RefreshToken.
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("select r from RefreshToken r where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    /** Sesiones vigentes: no revocadas y no expiradas. */
    @Query("select r from RefreshToken r "
            + "where r.userId = :userId "
            + "and r.revokedAt is null "
            + "and r.expiresAt > :now")
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
