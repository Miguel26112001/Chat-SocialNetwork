package com.my.enterprise.chat_backend.identity.infrastructure.persistence.jpa.repositories;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.UserRepository;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Username;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del puerto {@code UserRepository} sobre Spring Data JPA.
 * <p>
 * Traduce entre los value objects del dominio y los valores planos
 * que usan las consultas JPQL.
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public JpaUserRepository(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return jpaRepository.findByUsername(username.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return jpaRepository.existsByUsername(username.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByRole(UserRole role) {
        return jpaRepository.existsByRole(role);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
}
