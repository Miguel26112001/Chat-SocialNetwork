package com.my.enterprise.chat_backend.identity.infrastructure.persistence.jpa.repositories;

import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA del agregado User.
 * <p>
 * Las consultas son explícitas con JPQL (no derivadas por nombre) para
 * evitar ambigüedades con los @Embeddable records: se navega
 * directamente la componente {@code value} de cada value object.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where u.username.value = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select u from User u where u.email.value = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select count(u) > 0 from User u where u.username.value = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("select count(u) > 0 from User u where u.email.value = :email")
    boolean existsByEmail(@Param("email") String email);
}
