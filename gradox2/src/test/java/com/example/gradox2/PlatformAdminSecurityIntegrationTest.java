package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDegrees;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Separación entre el rol de gobernanza de un grado ({@code MASTER}) y el rol operativo de la plataforma
 * ({@code ADMIN}), exigida por D3 y D12.
 *
 * <p>Antes del multi-grado, {@code MASTER} daba acceso a {@code /admin/**}. Con masters por grado eso
 * habría dejado a cualquier master de cualquier grado administrando la plataforma entera.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformAdminSecurityIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger();

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

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        userRepository.deleteAll();
    }

    @Test
    void masterRoleDoesNotGrantAdminPanelAccess() throws Exception {
        createUser("masteruser", UserRole.MASTER);
        String token = loginAndGetToken("masteruser");

        mockMvc.perform(get("/admin/audit").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleGrantsAdminPanelAccess() throws Exception {
        createUser("adminuser", UserRole.ADMIN);
        String token = loginAndGetToken("adminuser");

        mockMvc.perform(get("/admin/audit").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void masterKeepsDegreeGovernanceAccess() throws Exception {
        createUser("governanceuser", UserRole.MASTER);
        String token = loginAndGetToken("governanceuser");

        // La gobernanza del grado sigue siendo MASTER, no ADMIN.
        mockMvc.perform(get("/promoteProposal/pending").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminIsNotAEligiblePromotionCandidate() throws Exception {
        createUser("opsuser", UserRole.ADMIN);
        String token = loginAndGetToken("opsuser");

        mockMvc.perform(post("/promoteProposal/request").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ROLE_OPERATION"));
    }

    @Test
    void profileExposesPlatformAdminFlag() throws Exception {
        createUser("adminprofile", UserRole.ADMIN);
        String token = loginAndGetToken("adminprofile");

        mockMvc.perform(get("/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.isPlatformAdmin").value(true))
                .andExpect(jsonPath("$.degreeCode").value(TestDegrees.INFORMATICA));
    }

    private void createUser(String username, UserRole role) {
        userRepository.save(User.builder()
                .username(username)
                .email(username + "@rai.usc.es")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(role)
                .degree(TestDegrees.informatica(degreeRepository))
                .enabled(true)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.get("isPlatformAdmin").asBoolean())
                .as("el login informa si el usuario administra la plataforma")
                .isEqualTo(node.get("role").asText().equals("ADMIN"));
        return node.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
