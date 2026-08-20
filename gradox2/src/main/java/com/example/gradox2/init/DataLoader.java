package com.example.gradox2.init;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.service.implementation.S3StorageService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"local", "demo"})
@RequiredArgsConstructor
public class DataLoader {
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                    SubjectRepository subjectRepository,
                                    VoteConfigRepository voteConfigRepository,
                                    FileRepository fileRepository,
                                    S3StorageService s3StorageService) {
        return args -> {
            boolean demoSeedsEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_DEMO_SEEDS", "false"));

            if (demoSeedsEnabled && userRepository.count() < 3) { // Solo carga si non hai usuarios (deixo 3 de margen)
                userRepository.save(User.builder()
                .username("juan123")
                .email("juan@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(10.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("maria456")
                .email("maria@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(50.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("pedro789")
                .email("pedro@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(5.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("laura321")
                .email("laura@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(7.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("carlos654")
                .email("carlos@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(20.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("sofia111")
                .email("sofia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(12.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("diego222")
                .email("diego@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(30.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("camila333")
                .email("camila@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(8.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("mateo444")
                .email("mateo@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(60.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("valentina555")
                .email("valentina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(15.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("lucas666")
                .email("lucas@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(5.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("martina777")
                .email("martina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(27.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("alejandro888")
                .email("alejandro@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(80.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("isabella999")
                .email("isabella@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(13.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("sebastian101")
                .email("sebastian@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(55.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("paula202")
                .email("paula@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(9.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("fernando303")
                .email("fernando@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(33.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("natalia404")
                .email("natalia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(17.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("andres505")
                .email("andres@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(6.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("carolina606")
                .email("carolina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(70.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("gonzalo707")
                .email("gonzalo@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(11.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("florencia808")
                .email("florencia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(65.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                userRepository.save(User.builder()
                .username("roberto909")
                .email("roberto@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(14.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                System.out.println("✅ Usuarios de prueba insertados en la base de datos.");


                userRepository.save(User.builder()
                .username("mariana010")
                .email("mariana@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(29.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .enabled(true)
                .build());

                // El catálogo de cursos y asignaturas lo siembra la migración Flyway V8.
                User demoUploader = userRepository.findByUsername("juan123").orElseThrow();
                Subject subject1 = subjectRepository.findByCode("FMAT1").orElse(null);
                if (subject1 == null) {
                    System.out.println("ℹ️ Catálogo de asignaturas no disponible; se omite el seed de archivos demo.");
                } else {
                    fileRepository.save(File.builder()
                        .title("Apuntes Algebra PUBLIC")
                        .description("Visibilidad pública")
                        .type(FileType.APUNTES)
                        .objectKey(s3StorageService.put("contenido algebra".getBytes()))
                        .fileHash("hash1")
                        .subject(subject1)
                        .uploader(demoUploader)
                        .visibilityLevel(FileVisibility.PUBLIC)
                        .build());

                    fileRepository.save(File.builder()
                        .title("Ejercicio Lengua RESTRICTED")
                        .description("Solo visible para USER y MASTER")
                        .type(FileType.EJERCICIO)
                        .objectKey(s3StorageService.put("contenido lengua".getBytes()))
                        .fileHash("hash2")
                        .subject(subject1)
                        .uploader(demoUploader)
                        .visibilityLevel(FileVisibility.RESTRICTED)
                        .build());

                    fileRepository.save(File.builder()
                        .title("Examen Privado PRIVATE")
                        .description("Solo visible para MASTER")
                        .type(FileType.EXAMEN)
                        .objectKey(s3StorageService.put("contenido examen".getBytes()))
                        .fileHash("hash3")
                        .subject(subject1)
                        .uploader(demoUploader)
                        .visibilityLevel(FileVisibility.PRIVATE)
                        .build());

                    System.out.println("✅ Archivos demo insertados con distintos niveles de visibilidad.");
                }
            } else if (!demoSeedsEnabled) {
                System.out.println("ℹ️ Seed de desarrollo desactivado por defecto. Usa ENABLE_DEMO_SEEDS=true para cargar datos demo.");
            }

            if (voteConfigRepository.count() == 0) {
                voteConfigRepository.save(
                    GlobalConfig.builder()
                        .quorumRequired(5)       // valor por defecto
                        .approvalThreshold(0.6)  // 60% de votos positivos
                        .maxPendingUploads(3)
                        .build()
                );
                System.out.println("✅ Configuración de votaciones inicial insertada en la base de datos.");
            }
        };
    }
}
