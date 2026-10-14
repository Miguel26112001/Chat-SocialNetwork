package com.my.enterprise.chat_backend.identity.infrastructure.bootstrap;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.UserRepository;
import com.my.enterprise.chat_backend.identity.domain.model.aggregates.User;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.Email;
import com.my.enterprise.chat_backend.identity.domain.model.valueobjects.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bootstrap del primer administrador.
 *
 * Resuelve el problema del huevo y la gallina: sin admins no hay
 * quien promueva, así que en el arranque —solo si no existe NINGÚN
 * admin— se promueve la cuenta indicada en
 * {@code app.identity.initial-admin-email}.
 *
 * Se activa únicamente con la propiedad definida
 * ({@code INITIAL_ADMIN_EMAIL}); con valor vacío no hace nada.
 * Es idempotente y de un solo uso: en cuanto existe un admin,
 * nunca vuelve a actuar.
 *
 * Actúa como actor del sistema (no como petición de usuario), por eso
 * opera directamente sobre el agregado vía puertos, sin pasar por
 * los command services (que exigen un solicitante admin).
 */
@Component
@Order(Integer.MAX_VALUE)
@ConditionalOnProperty(name = "app.identity.initial-admin-email", matchIfMissing = false)
public class InitialAdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminBootstrap.class);

    private final UserRepository userRepository;
    private final Clock clock;
    private final String initialAdminEmail;

    public InitialAdminBootstrap(UserRepository userRepository,
                                 Clock clock,
                                 @Value("${app.identity.initial-admin-email:}") String initialAdminEmail) {
        this.userRepository = userRepository;
        this.clock = clock;
        this.initialAdminEmail = initialAdminEmail.trim();
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (initialAdminEmail.isBlank()) {
            return;
        }
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.info("Ya existe un administrador; se omite el bootstrap inicial");
            return;
        }
        User user = userRepository.findByEmail(new Email(initialAdminEmail)).orElse(null);
        if (user == null) {
            log.warn("Bootstrap inicial omitido: no existe la cuenta '{}'", initialAdminEmail);
            return;
        }
        user.promoteToAdmin(clock.now());
        userRepository.save(user);
        log.info("Cuenta '{}' promovida a ADMIN (bootstrap inicial)", initialAdminEmail);
    }
}
