package com.my.enterprise.chat_backend.identity.domain.model.aggregates;

import com.my.enterprise.chat_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raíz del contexto Identity.
 * <p>
 * Responsabilidad: una sesión persistente de un dispositivo.
 * Permite renovar JWTs, cerrar sesiones y gestionar múltiples
 * dispositivos (especificación: tabla refresh_tokens).
 * <p>
 * Mapeado JPA directo: la tabla identity.refresh_tokens de la V1.
 * <p>
 * REGLA DE ORO: jamás se almacena el refresh token en texto plano,
 * solo su HASH irreversible ({@code token_hash}); el hash se genera
 * en infraestructura mediante el puerto {@code TokenHasher}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = "refresh_tokens",
        schema = "identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_identity_refresh_tokens_hash", columnNames = "token_hash")
        })
public class RefreshToken extends AuditableAbstractAggregateRoot<RefreshToken> {

    /** Duración por defecto de un refresh token: 30 días. */
    public static final Duration DEFAULT_TTL = Duration.ofDays(30);

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshToken() {
        // Requerido por JPA; los atributos los rehidrata Hibernate.
    }

    private RefreshToken(UUID userId, String tokenHash, Instant expiresAt, Instant now) {
        this.id = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId, "RefreshToken: userId no puede ser nulo");
        this.tokenHash = Objects.requireNonNull(tokenHash, "RefreshToken: tokenHash no puede ser nulo");
        this.expiresAt = Objects.requireNonNull(expiresAt, "RefreshToken: expiresAt no puede ser nulo");
        markCreated(now);
    }

    /** Emite una nueva sesión para un usuario. */
    public static RefreshToken issue(UUID userId, String tokenHash, Instant now) {
        return new RefreshToken(userId, tokenHash, now.plus(DEFAULT_TTL), now);
    }

    /** Emite una nueva sesión con TTL personalizado. */
    public static RefreshToken issue(UUID userId, String tokenHash, Duration ttl, Instant now) {
        Objects.requireNonNull(ttl, "RefreshToken: ttl no puede ser nulo");
        return new RefreshToken(userId, tokenHash, now.plus(ttl), now);
    }

    /** Revoca la sesión (logout / "cerrar sesión en todos los dispositivos"). Idempotente. */
    public void revoke(Instant now) {
        if (isRevoked()) {
            return;
        }
        this.revokedAt = now;
        touch(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    /** Solo un token vigente puede usarse para renovar la sesión. */
    public boolean isActive(Instant now) {
        return !isRevoked() && !isExpired(now);
    }
}
