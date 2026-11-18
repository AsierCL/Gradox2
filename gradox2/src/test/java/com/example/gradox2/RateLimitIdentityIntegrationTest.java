package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Identidad del rate limiting detrás del proxy de Render (WAVE1 S3, S4).
 *
 * <p>Dos supuestos se prueban acá: la IP sale del último salto que agrega el proxy de confianza, y las
 * peticiones sin sesión no comparten un bucket de votación entre sí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.rate-limit.trusted-proxy-hops=1")
class RateLimitIdentityIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger(9700);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRateLimiter rateLimiter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        databaseCleaner.clean();
    }

    @Test
    void spoofedForwardedForPrefixDoesNotBuyFreshLoginAttempts() throws Exception {
        String realClientIp = "198.51.100." + IP_COUNTER.incrementAndGet();

        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            // El cliente rota la IP falsa en cada intento; el proxy agrega la real al final.
                            .header("X-Forwarded-For", "10.9.9." + attempt + ", " + realClientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("username", "nobody", "password", "wrong-password"))))
                    .andExpect(status().isNotFound());
        }

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "10.9.9.99, " + realClientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "nobody", "password", "wrong-password"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void anonymousVoteRequestsDoNotConsumeASharedBucket() throws Exception {
        // Antes, todas las peticiones sin sesión caían en "vote:anonymous" y se bloqueaban entre sí.
        for (int attempt = 1; attempt <= 25; attempt++) {
            String clientIp = "10.40.0." + attempt;
            mockMvc.perform(post("/vote/{id}/{inFavor}", 1, false)
                            .with(request -> {
                                request.setRemoteAddr(clientIp);
                                return request;
                            }))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void authenticatedVoteBucketIsPerUser() throws Exception {
        createUser("voterone");
        createUser("votertwo");
        String first = loginAndGetToken("voterone");
        String second = loginAndGetToken("votertwo");

        for (int attempt = 1; attempt <= 20; attempt++) {
            String clientIp = "10.50.0." + attempt;
            mockMvc.perform(post("/vote/{id}/{inFavor}", 1, false)
                            .header("Authorization", "Bearer " + first)
                            .with(request -> {
                                request.setRemoteAddr(clientIp);
                                return request;
                            }))
                    .andExpect(status().isNotFound());
        }

        // El primer usuario agotó su propio cupo; el segundo no tiene por qué pagarlo.
        mockMvc.perform(post("/vote/{id}/{inFavor}", 1, false)
                        .header("Authorization", "Bearer " + first)
                        .with(request -> {
                            request.setRemoteAddr("10.50.0.200");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(post("/vote/{id}/{inFavor}", 1, false)
                        .header("Authorization", "Bearer " + second)
                        .with(request -> {
                            request.setRemoteAddr("10.50.0.201");
                            return request;
                        }))
                .andExpect(status().isNotFound());

        assertThat(rateLimiter.trackedBuckets()).as("buckets acotados").isLessThanOrEqualTo(10_000);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User createUser(String username) {
        Degree degree = TestDegrees.informatica(degreeRepository);
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@rai.usc.es")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(UserRole.USER)
                .degree(degree)
                .enabled(true)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.60.0." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
