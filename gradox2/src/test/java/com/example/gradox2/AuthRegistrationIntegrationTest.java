package com.example.gradox2;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VerificationToken;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.utils.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.base-url=https://gradox.test")
class AuthRegistrationIntegrationTest {

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

        @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
                passwordResetTokenRepository.deleteAll();
                refreshTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerShouldRejectDuplicateUsername() throws Exception {
        User existing = User.builder()
                .username("dupeuser")
                .email("other@rai.usc.es")
                .passwordHash("hash")
                .enabled(true)
                .role(UserRole.USER)
                .build();
        userRepository.save(existing);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "dupeuser",
                                "email", "newmail@rai.usc.es",
                                "password", "SecurePass1!"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_EXIST_ERROR"));
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {
        User existing = User.builder()
                .username("olduser")
                .email("dupe@rai.usc.es")
                .passwordHash("hash")
                .enabled(true)
                .role(UserRole.USER)
                .build();
        userRepository.save(existing);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "newuser",
                                "email", "dupe@rai.usc.es",
                                "password", "SecurePass1!"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_EXIST_ERROR"));
    }

    @Test
    void registerShouldPersistDisabledUserAndSendVerificationWithConfiguredBaseUrl() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "freshuser",
                                "email", "fresh@rai.usc.es",
                                "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("freshuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        User created = userRepository.findByUsername("freshuser").orElseThrow();
        VerificationToken token = verificationTokenRepository.findAll().stream().findFirst().orElseThrow();

        org.junit.jupiter.api.Assertions.assertFalse(created.isEnabled());
        org.junit.jupiter.api.Assertions.assertEquals(created.getId(), token.getUser().getId());

        verify(emailService).sendEmail(
                eq("fresh@rai.usc.es"),
                anyString(),
                org.mockito.ArgumentMatchers.contains("https://gradox.test/api/auth/verify?token="));
    }

    @Test
    void verifyShouldEnableUserForValidToken() throws Exception {
        User user = User.builder()
                .username("pending")
                .email("pending@rai.usc.es")
                .passwordHash("hash")
                .enabled(false)
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        VerificationToken token = new VerificationToken("valid-token", user);
        verificationTokenRepository.save(token);

        mockMvc.perform(get("/api/auth/verify").param("token", "valid-token"))
                .andExpect(status().isOk());

        User refreshed = userRepository.findByUsername("pending").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(refreshed.isEnabled());
        org.junit.jupiter.api.Assertions.assertTrue(verificationTokenRepository.findByToken("valid-token").isEmpty());
    }

    @Test
    void verifyShouldRejectExpiredTokenAndDeleteIt() throws Exception {
        User user = User.builder()
                .username("expireduser")
                .email("expired@rai.usc.es")
                .passwordHash("hash")
                .enabled(false)
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        VerificationToken token = new VerificationToken("expired-token", user);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        verificationTokenRepository.save(token);

        mockMvc.perform(get("/api/auth/verify").param("token", "expired-token"))
                .andExpect(status().isBadRequest());

        User refreshed = userRepository.findByUsername("expireduser").orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(refreshed.isEnabled());
        org.junit.jupiter.api.Assertions.assertTrue(verificationTokenRepository.findByToken("expired-token").isEmpty());
    }

    @Test
    void verifyShouldRejectUnknownToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify").param("token", "missing-token"))
                .andExpect(status().isBadRequest());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
