package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

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
import com.example.gradox2.support.TestDatabaseCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contrato de autenticación que consume el frontend: las operaciones abiertas
 * responden a un GUEST, las protegidas devuelven 401 (no 403) cuando falta el
 * token o es inválido, y 403 solo cuando hay usuario sin el rol necesario.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthErrorContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @Test
    void missingTokenReturns401WithTheErrorEnvelope() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void invalidTokenReturns401WithTheErrorEnvelope() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    @Test
    void authenticatedUserWithoutTheRoleGets403WithTheErrorEnvelope() throws Exception {
        String token = createUserAndLogin("plainuser", "plain@rai.usc.es", UserRole.USER);

        mockMvc.perform(get("/admin/users/banned").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void guestCanReadTheOpenEndpoints() throws Exception {
        mockMvc.perform(get("/subjects")).andExpect(status().isOk());
        mockMvc.perform(get("/courses")).andExpect(status().isOk());
        mockMvc.perform(get("/badges")).andExpect(status().isOk());
        mockMvc.perform(get("/stats")).andExpect(status().isOk());
        mockMvc.perform(get("/health")).andExpect(status().isOk());
        mockMvc.perform(get("/files/all")).andExpect(status().isOk());
    }

    @Test
    void writingWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/files/{id}/vote/{upvote}", 1, true))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    private String createUserAndLogin(String username, String email, UserRole role) throws Exception {
        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode("SecurePass1!"))
                .enabled(true)
                .role(role)
                .build());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
