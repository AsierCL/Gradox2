package com.example.gradox2.init;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Crea el primer usuario MASTER en produccion a partir de las variables de
 * entorno BOOTSTRAP_MASTER_USERNAME, BOOTSTRAP_MASTER_PASSWORD y
 * BOOTSTRAP_MASTER_EMAIL. Es idempotente: si el usuario ya existe, no hace nada.
 * La contrasena nunca se escribe en los logs.
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
public class MasterBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MasterBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String username = System.getenv("BOOTSTRAP_MASTER_USERNAME");
        String password = System.getenv("BOOTSTRAP_MASTER_PASSWORD");
        String email = System.getenv("BOOTSTRAP_MASTER_EMAIL");

        if (isBlank(username) || isBlank(password) || isBlank(email)) {
            log.info("BOOTSTRAP_MASTER_USERNAME/PASSWORD/EMAIL no definidos; se omite la creacion del primer MASTER.");
            return;
        }

        if (userRepository.findByUsername(username).isPresent()
                || userRepository.findByEmail(email).isPresent()) {
            log.info("El usuario MASTER de arranque ya existe; no se modifica.");
            return;
        }

        userRepository.save(User.builder()
                .username(username.trim())
                .email(email.trim())
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.MASTER)
                .reputation(100.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

        log.info("Primer usuario MASTER '{}' creado durante el arranque.", username);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
