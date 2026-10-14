package com.my.enterprise.chat_backend.identity.domain.model.aggregates;

import com.my.enterprise.chat_backend.identity.domain.exceptions.InactiveUserException;
import com.my.enterprise.chat_backend.identity.domain.model.events.PasswordChangedEvent;
import com.my.enterprise.chat_backend.identity.domain.model.events.UserDeactivatedEvent;
import com.my.enterprise.chat_backend.identity.domain.model.events.UserRegisteredEvent;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.PasswordHash;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import com.my.enterprise.chat_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Agregado raíz del contexto Identity.
 *
 * Responsabilidad: representar una cuenta de usuario —quién es y cómo
 * se autentica—. NO contiene datos de presentación (displayName, bio,
 * avatar, is_private): esos viven en el bounded context User Profile.
 *
 * Mapeado JPA directo: la tabla identity.users de la migración V1.
 * La unicidad de username/email también se declara aquí como
 * {@code @UniqueConstraint} (doble defensa: capa de dominio + BD).
 *
 * Invariantes que este agregado protege:
 *  - el username y el correo cumplen su formato (garantizado por los VOs);
 *  - solo la propia cuenta puede cambiar su contraseña;
 *  - una cuenta desactivada no puede autenticarse.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
        name = "users",
        schema = "identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_identity_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uq_identity_users_email", columnNames = "email")
        })
public class User extends AuditableAbstractAggregateRoot<User> {

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, length = 50))
    private Username username;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, length = 255))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password_hash", nullable = false, length = 255))
    private PasswordHash passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected User() {
        // Requerido por JPA; los atributos los rehidrata Hibernate.
    }

    private User(UUID id, Username username, Email email, PasswordHash passwordHash, Instant now) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = true;
        markCreated(now);
    }

    /**
     * Fábrica de registro. Única forma de crear un User.
     * Publica {@link UserRegisteredEvent} para que otros contextos
     * (p. ej. User Profile) creen su perfil de forma asíncrona.
     */
    public static User register(Username username, Email email, PasswordHash passwordHash, Instant now) {
        User user = new User(UUID.randomUUID(), username, email, passwordHash, now);
        user.registerEvent(new UserRegisteredEvent(user.id, username, email, now));
        return user;
    }

    /**
     * Cambio de contraseña. La validación de la contraseña en texto
     * plano y su comparación con la actual ocurren en la aplicación
     * (política de dominio + puerto PasswordHasher); aquí solo se
     * registra el resultado.
     */
    public void changePassword(PasswordHash newPassword, Instant now) {
        this.passwordHash = newPassword;
        touch(now);
        registerEvent(new PasswordChangedEvent(this.id, now));
    }

    /** Desactiva la cuenta. Idempotente: desactivar dos veces no hace nada. */
    public void deactivate(Instant now) {
        if (!this.active) {
            return;
        }
        this.active = false;
        touch(now);
        registerEvent(new UserDeactivatedEvent(this.id, now));
    }

    /** Reactiva la cuenta. */
    public void activate(Instant now) {
        if (this.active) {
            return;
        }
        this.active = true;
        touch(now);
    }

    /**
     * Guardia de autenticación: la capa de aplicación la invoca antes
     * de verificar la contraseña para descartar cuentas desactivadas
     * sin revelar que la contraseña era correcta.
     */
    public void ensureCanAuthenticate() {
        if (!this.active) {
            throw new InactiveUserException(username.value());
        }
    }
}
