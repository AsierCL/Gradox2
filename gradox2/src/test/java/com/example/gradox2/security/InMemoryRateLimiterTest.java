package com.example.gradox2.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Cota del rate limiter en memoria (WAVE1 S4).
 *
 * <p>Render corre un contenedor con memoria acotada y una sola instancia. El mapa de buckets no puede
 * crecer sin techo: cada IP nueva dejaba una entrada viva para siempre.
 */
class InMemoryRateLimiterTest {

    @Test
    void allowsUpToTheLimitInsideTheWindowAndThenBlocks() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(100, 60_000);

        for (int attempt = 1; attempt <= 3; attempt++) {
            assertThat(limiter.isAllowed("login:1.2.3.4", 3, 60_000))
                    .as("intento %s dentro del límite", attempt)
                    .isTrue();
        }

        assertThat(limiter.isAllowed("login:1.2.3.4", 3, 60_000)).isFalse();
        assertThat(limiter.isAllowed("login:5.6.7.8", 3, 60_000))
                .as("el límite es por clave, no global")
                .isTrue();
    }

    @Test
    void freesTheWindowOnceItExpires() throws Exception {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(100, 60_000);

        assertThat(limiter.isAllowed("login:1.2.3.4", 1, 20)).isTrue();
        assertThat(limiter.isAllowed("login:1.2.3.4", 1, 20)).isFalse();

        Thread.sleep(40);

        assertThat(limiter.isAllowed("login:1.2.3.4", 1, 20)).isTrue();
    }

    @Test
    void neverTracksMoreBucketsThanConfigured() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(10, 600_000);

        for (int ip = 0; ip < 500; ip++) {
            limiter.isAllowed("login:10.0.0." + ip, 5, 60_000);
        }

        assertThat(limiter.trackedBuckets()).isLessThanOrEqualTo(10);
    }
}
