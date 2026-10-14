package com.my.enterprise.chat_backend.shared.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;

/**
 * Mapeado JPA base de todos los agregados raíz del sistema.
 * <p>
 * Combina:
 *  - el mecanismo de eventos de dominio de Spring Data
 *    ({@link AbstractAggregateRoot}), que permite hacer
 *    {@code registerEvent(...)} y publicarlos con
 *    {@code publishDomainEvents()} al hacer flush/save;
 *  - los campos de auditoría comunes (created_at / updated_at)
 *    que comparten todas las tablas de la especificación.
 * <p>
 * Los campos de Spring Data {@code AbstractAggregateRoot} no se
 * persisten: su superclase no es entidad ni MappedSuperclass, y la
 * colección de eventos es transient.
 * <p>
 * Parámetro de tipo: la propia subclase, para encadenar tipos
 * (patrón CRTP), p. ej. {@code class User extends AuditableAbstractAggregateRoot<User>}.
 * <p>
 * @param <S> el agregado concreto que extiende esta clase
 */
@Getter
@MappedSuperclass
public abstract class AuditableAbstractAggregateRoot<S extends AuditableAbstractAggregateRoot<S>>
        extends AbstractAggregateRoot<S> {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AuditableAbstractAggregateRoot() {
    }

    protected AuditableAbstractAggregateRoot(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

  /**
     * Marca el nacimiento del agregado. Solo debe llamarse una vez,
     * desde la fábrica estática del agregado.
     */
    protected void markCreated(Instant now) {
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Marca una modificación del agregado.
     */
    protected void touch(Instant now) {
        this.updatedAt = now;
    }
}
