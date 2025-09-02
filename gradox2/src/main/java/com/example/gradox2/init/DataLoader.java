package com.example.gradox2.init;
import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataLoader {
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, FileRepository fileRepository, SubjectRepository subjectRepository, CourseRepository courseRepository) {
        return args -> {
            if (userRepository.count() < 3) { // Solo carga si non hai usuarios (deixo 3 de margen)
                userRepository.save(User.builder()
                .username("juan123")
                .email("juan@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(10.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("maria456")
                .email("maria@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(50.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("pedro789")
                .email("pedro@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(5.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("laura321")
                .email("laura@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(7.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("carlos654")
                .email("carlos@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(20.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("sofia111")
                .email("sofia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(12.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("diego222")
                .email("diego@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(30.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("camila333")
                .email("camila@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(8.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("mateo444")
                .email("mateo@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(60.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("valentina555")
                .email("valentina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(15.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("lucas666")
                .email("lucas@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(5.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("martina777")
                .email("martina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(27.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("alejandro888")
                .email("alejandro@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(80.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("isabella999")
                .email("isabella@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(13.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("sebastian101")
                .email("sebastian@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(55.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("paula202")
                .email("paula@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(9.5)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("fernando303")
                .email("fernando@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(33.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("natalia404")
                .email("natalia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(17.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("andres505")
                .email("andres@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(6.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("carolina606")
                .email("carolina@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(70.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("gonzalo707")
                .email("gonzalo@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(11.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("florencia808")
                .email("florencia@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(65.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("roberto909")
                .email("roberto@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .reputation(14.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                userRepository.save(User.builder()
                .username("mariana010")
                .email("mariana@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.MASTER)
                .reputation(29.0)
                .createdAt(Instant.now())
                .lastLogin(Instant.now())
                .build());

                Course primero = courseRepository.save(Course.builder()
                    .name("Primer curso")
                    .code("1")
                    .build());

                Course segundo = courseRepository.save(Course.builder()
                    .name("Segundo curso")
                    .code("2")
                    .build());

                subjectRepository.saveAll(java.util.List.of(
                    // Subjects for "Primer curso"
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Matemáticas I")
                        .code("MAT1")
                        .course(primero)
                        .build(),
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Lengua Castellana I")
                        .code("LEN1")
                        .course(primero)
                        .build(),
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Ciencias Naturales I")
                        .code("CNA1")
                        .course(primero)
                        .build(),
                    // Subjects for "Segundo curso"
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Matemáticas II")
                        .code("MAT2")
                        .course(segundo)
                        .build(),
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Lengua Castellana II")
                        .code("LEN2")
                        .course(segundo)
                        .build(),
                    com.example.gradox2.persistence.entities.Subject.builder()
                        .name("Ciencias Sociales II")
                        .code("CSO2")
                        .course(segundo)
                        .build()
                ));

                System.out.println("✅ Cursos de prueba insertados en la base de datos.");

                System.out.println("✅ Usuarios de prueba insertados en la base de datos.");
            }
        };
    }
}
