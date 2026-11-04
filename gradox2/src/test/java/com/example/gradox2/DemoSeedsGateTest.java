package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.gradox2.init.DataLoader;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.service.implementation.S3StorageService;

/**
 * Los datos demo siembran usuarios con contraseñas conocidas, dos de ellos
 * MASTER. El seeding tiene que exigir activación explícita por propiedad, no
 * solo por perfil, para que un despliegue con el perfil equivocado no llene una
 * base real.
 */
class DemoSeedsGateTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().setActiveProfiles("local"))
            .withUserConfiguration(DataLoader.class)
            .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
            .withBean(UserRepository.class, () -> mock(UserRepository.class))
            .withBean(SubjectRepository.class, () -> mock(SubjectRepository.class))
            .withBean(VoteConfigRepository.class, () -> mock(VoteConfigRepository.class))
            .withBean(FileRepository.class, () -> mock(FileRepository.class))
            .withBean(S3StorageService.class, () -> mock(S3StorageService.class));

    @Test
    void doesNotSeedWhenTheFlagIsMissing() {
        runner.run(context -> assertThat(context).doesNotHaveBean("initDatabase"));
    }

    @Test
    void doesNotSeedWhenTheFlagIsDisabled() {
        runner.withPropertyValues("app.demo-seeds.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean("initDatabase"));
    }

    @Test
    void seedsOnlyWhenExplicitlyEnabled() {
        runner.withPropertyValues("app.demo-seeds.enabled=true")
                .run(context -> assertThat(context).hasBean("initDatabase"));
    }
}
