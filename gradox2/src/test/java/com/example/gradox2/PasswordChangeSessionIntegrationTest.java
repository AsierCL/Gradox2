package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cambiar la contraseña cierra todas las sesiones abiertas (WAVE1 S2).
 *
 * <p>Antes, un token robado seguía sirviendo después de que la víctima cambiara la contraseña: el hash
 * cambiaba pero {@code token_version} no, y el refresh token seguía vivo. Es el escenario que el
 * versionado de tokens existe para cubrir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordChangeSessionIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final String NEW_PASSWORD = "NewSecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger(9400);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRateLimiter rateLimiter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private User user;

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        databaseCleaner.clean();
        user = createUser("sessionuser");
    }

    @Test
    void passwordChangeRevokesAccessAndRefreshTokens() throws Exception {
        JsonNode session = login(PASSWORD);
        String accessToken = session.get("token").asText();
        String refreshToken = session.get("refreshToken").asText();

        // Sesión válida antes del cambio.
        mockMvc.perform(get("/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", NEW_PASSWORD))))
                .andExpect(status().isOk());

        // El access token anterior ya no sirve…
        mockMvc.perform(get("/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));

        // …y el refresh token tampoco.
        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));

        assertThat(refreshTokenRepository.findAllByUser(userRepository.findById(user.getId()).orElseThrow()))
                .as("no debe quedar ningún refresh token vigente")
                .isEmpty();

        // La contraseña nueva funciona y habilita una sesión nueva.
        String newToken = login(NEW_PASSWORD).get("token").asText();
        mockMvc.perform(get("/users/me").header("Authorization", bearer(newToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sessionuser"));
    }

    @Test
    void profileEditWithoutPasswordChangeKeepsTheCurrentSessionAlive() throws Exception {
        JsonNode session = login(PASSWORD);
        String accessToken = session.get("token").asText();

        // Misma contraseña (no se envía): el token_version no debe moverse.
        mockMvc.perform(put("/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "sessionuser"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sessionuser"));
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

    private JsonNode login(String password) throws Exception {
        String ip = "10.30.40." + IP_COUNTER.incrementAndGet();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", user.getUsername(), "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
