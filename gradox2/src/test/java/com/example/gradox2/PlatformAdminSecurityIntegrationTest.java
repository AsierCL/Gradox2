package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.AuditRecordRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
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

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        databaseCleaner.clean();
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
                // Lombok + Jackson pueden duplicar el par campo/getter: el contrato tiene un solo nombre.
                .andExpect(jsonPath("$.platformAdmin").doesNotExist())
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

    // ── override administrativo de grado (D2, §4.3) ──────────────────────────

    @Test
    void adminCanChangeUserDegreeAndInvalidatesOpenSessions() throws Exception {
        createUser("moveduser", UserRole.USER);
        String oldToken = loginAndGetToken("moveduser");
        createUser("opsadmin", UserRole.ADMIN);
        String adminToken = loginAndGetToken("opsadmin");

        long quimicaId = TestDegrees.quimica(degreeRepository).getId();
        long movedId = userRepository.findByUsername("moveduser").orElseThrow().getId();

        mockMvc.perform(patch("/admin/users/{id}/degree", movedId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "degreeId", quimicaId,
                                "reason", "Se registró en el grado equivocado"))))
                .andExpect(status().isOk());

        User moved = userRepository.findByUsername("moveduser").orElseThrow();
        assertThat(moved.getDegree().getCode()).isEqualTo(TestDegrees.QUIMICA);
        assertThat(moved.getTokenVersion())
                .as("el cambio de grado rota las sesiones vigentes").isEqualTo(2);

        mockMvc.perform(get("/users/me").header("Authorization", bearer(oldToken)))
                .andExpect(status().isUnauthorized());

        assertThat(auditRecordRepository.findAll())
                .filteredOn(a -> a.getActionType() == ActionType.DEGREE_CHANGE)
                .as("el cambio de grado queda auditado con actor y motivo")
                .hasSize(1)
                .allSatisfy(a -> {
                    assertThat(a.getActor().getUsername()).isEqualTo("opsadmin");
                    assertThat(a.getDetails()).contains("G4012").contains("G4022")
                            .contains("Se registró en el grado equivocado");
                });
    }

    @Test
    void masterCannotChangeAUserDegree() throws Exception {
        createUser("targetuser", UserRole.USER);
        createUser("masteractor", UserRole.MASTER);
        String masterToken = loginAndGetToken("masteractor");

        long quimicaId = TestDegrees.quimica(degreeRepository).getId();
        long targetId = userRepository.findByUsername("targetuser").orElseThrow().getId();

        mockMvc.perform(patch("/admin/users/{id}/degree", targetId)
                        .header("Authorization", bearer(masterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "degreeId", quimicaId,
                                "reason", "intento"))))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByUsername("targetuser").orElseThrow().getDegree().getCode())
                .as("el grado no cambia si el actor no es ADMIN").isEqualTo(TestDegrees.INFORMATICA);
    }

    @Test
    void changingToTheSameDegreeIsRejected() throws Exception {
        createUser("sameuser", UserRole.USER);
        createUser("opsadmin2", UserRole.ADMIN);
        String adminToken = loginAndGetToken("opsadmin2");

        long informaticaId = TestDegrees.informatica(degreeRepository).getId();
        long sameId = userRepository.findByUsername("sameuser").orElseThrow().getId();

        mockMvc.perform(patch("/admin/users/{id}/degree", sameId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "degreeId", informaticaId,
                                "reason", "sin cambio"))))
                .andExpect(status().isConflict());
    }

    @Test
    void degreeChangeRequiresAReason() throws Exception {
        createUser("noreasonuser", UserRole.USER);
        createUser("opsadmin3", UserRole.ADMIN);
        String adminToken = loginAndGetToken("opsadmin3");

        long quimicaId = TestDegrees.quimica(degreeRepository).getId();
        long userId = userRepository.findByUsername("noreasonuser").orElseThrow().getId();

        mockMvc.perform(patch("/admin/users/{id}/degree", userId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "degreeId", quimicaId,
                                "reason", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        assertThat(userRepository.findByUsername("noreasonuser").orElseThrow().getDegree().getCode())
                .isEqualTo(TestDegrees.INFORMATICA);
    }

    @Test
    void profileUpdateCannotChangeTheDegree() throws Exception {
        createUser("studentuser", UserRole.USER);
        String token = loginAndGetToken("studentuser");
        long quimicaId = TestDegrees.quimica(degreeRepository).getId();

        mockMvc.perform(put("/users/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "studentuser",
                                "degreeId", quimicaId))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_IMMUTABLE"));

        assertThat(userRepository.findByUsername("studentuser").orElseThrow().getDegree().getCode())
                .as("el grado almacenado no cambia").isEqualTo(TestDegrees.INFORMATICA);
    }
}
