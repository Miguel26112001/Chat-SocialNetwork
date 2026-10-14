package com.my.enterprise.chat_backend.identity.application.outboundservices.ports;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia del agregado User.
 * <p>
 * Interfaz pura en la capa de aplicación: la implementación vive en
 * infrastructure.persistence.jpa.repositories y traduce entre el
 * modelo de dominio y JPA.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(Username username);

    Optional<User> findByEmail(Email email);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);

    /**
     * Guarda (inserta o actualiza) el agregado.
     * Spring Data publica automáticamente los eventos de dominio
     * registrados en el agregado al llamar a save().
     */
    User save(User user);
}
