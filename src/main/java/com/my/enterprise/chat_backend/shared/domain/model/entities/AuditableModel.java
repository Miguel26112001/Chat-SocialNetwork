package com.my.enterprise.chat_backend.shared.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.Instant;

/**
 * Mapeado JPA base de las entidades (objetos con identidad) que NO
 * son agregados raíz, p. ej. hijos de un agregado.
 * <p>
 * Proporciona los mismos campos de auditoría que
 * {@code AuditableAbstractAggregateRoot} para mantener una
 * única convención en todo el sistema.
 */
@Getter
@MappedSuperclass
public abstract class AuditableModel {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AuditableModel() {
    }

    protected AuditableModel(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

  protected void markCreated(Instant now) {
        this.createdAt = now;
        this.updatedAt = now;
    }

    protected void touch(Instant now) {
        this.updatedAt = now;
    }
}
