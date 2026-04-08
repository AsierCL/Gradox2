package com.example.gradox2;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.PasswordResetToken;
import com.example.gradox2.persistence.entities.RefreshToken;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.utils.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.base-url=https://gradox.test")
class AuthSessionIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(500);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

        @Autowired
        private FileProposalRepository fileProposalRepository;

        @Autowired
        private ProposalRepository proposalRepository;

        @Autowired
        private ScoreRepository scoreRepository;

        @Autowired
        private FileRepository fileRepository;

        @Autowired
        private TempFileRepository tempFileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
                scoreRepository.deleteAll();
                fileProposalRepository.deleteAll();
                proposalRepository.deleteAll();
                fileRepository.deleteAll();
                tempFileRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginShouldReturnAccessAndRefreshTokensAndPersistRefreshSession() throws Exception {
        createEnabledUser("sessionuser", "session@rai.usc.es", "SecurePass1!", UserRole.USER);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "sessionuser", "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("sessionuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshToken = response.get("refreshToken").asText();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(storedToken.isRevoked());
        org.junit.jupiter.api.Assertions.assertEquals("sessionuser", storedToken.getUser().getUsername());
    }

    @Test
    void refreshShouldRotateRefreshTokenAndInvalidatePreviousOne() throws Exception {
        createEnabledUser("rotator", "rotator@rai.usc.es", "SecurePass1!", UserRole.USER);
        JsonNode login = login("rotator", "SecurePass1!");
        String oldRefreshToken = login.get("refreshToken").asText();

        MvcResult result = mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("rotator"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode refreshed = objectMapper.readTree(result.getResponse().getContentAsString());
        String newRefreshToken = refreshed.get("refreshToken").asText();

        org.junit.jupiter.api.Assertions.assertNotEquals(oldRefreshToken, newRefreshToken);
        org.junit.jupiter.api.Assertions.assertTrue(refreshTokenRepository.findByToken(oldRefreshToken).orElseThrow().isRevoked());
        org.junit.jupiter.api.Assertions.assertFalse(refreshTokenRepository.findByToken(newRefreshToken).orElseThrow().isRevoked());

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    @Test
    void logoutShouldRevokeRefreshTokenAndBlockReuse() throws Exception {
        createEnabledUser("logoutuser", "logout@rai.usc.es", "SecurePass1!", UserRole.USER);
        String refreshToken = login("logoutuser", "SecurePass1!").get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertTrue(refreshTokenRepository.findByToken(refreshToken).orElseThrow().isRevoked());

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    @Test
    void requestPasswordResetShouldStoreTokenAndSendEmail() throws Exception {
        createEnabledUser("resetuser", "reset@rai.usc.es", "SecurePass1!", UserRole.USER);

        mockMvc.perform(post("/api/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "reset@rai.usc.es"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Si el correo existe, se enviará un enlace de restablecimiento"));

        PasswordResetToken token = passwordResetTokenRepository.findAll().stream().findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("resetuser", token.getUser().getUsername());

        verify(emailService).sendEmail(
                eq("reset@rai.usc.es"),
                anyString(),
                org.mockito.ArgumentMatchers.contains("https://gradox.test/api/auth/password/reset?token="));
    }

    @Test
    void requestPasswordResetShouldBeSilentForUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "missing@rai.usc.es"))))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertTrue(passwordResetTokenRepository.findAll().isEmpty());
        verifyNoInteractions(emailService);
    }

    @Test
    void resetPasswordShouldChangePasswordAndInvalidateSessions() throws Exception {
        createEnabledUser("changeme", "changeme@rai.usc.es", "SecurePass1!", UserRole.USER);
        String oldRefreshToken = login("changeme", "SecurePass1!").get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "changeme@rai.usc.es"))))
                .andExpect(status().isOk());

        PasswordResetToken token = passwordResetTokenRepository.findAll().stream().findFirst().orElseThrow();
        String resetToken = token.getToken();

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", "NewSecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Contraseña actualizada correctamente"));

        org.junit.jupiter.api.Assertions.assertTrue(passwordResetTokenRepository.findByToken(resetToken).isEmpty());
        User updatedUser = userRepository.findByUsername("changeme").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(refreshTokenRepository.findAllByUser(updatedUser).isEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "changeme", "password", "SecurePass1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "changeme", "password", "NewSecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    @Test
    void resetPasswordShouldRejectUnknownToken() throws Exception {
        createEnabledUser("invalidreset", "invalidreset@rai.usc.es", "SecurePass1!", UserRole.USER);

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", "missing-token", "newPassword", "NewSecurePass1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    private User createEnabledUser(String username, String email, String password, UserRole role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private JsonNode login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", nextTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String nextTestIp() {
        return "198.51.100." + IP_COUNTER.incrementAndGet();
    }
}
