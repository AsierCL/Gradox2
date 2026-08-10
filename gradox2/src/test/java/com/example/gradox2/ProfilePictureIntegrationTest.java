package com.example.gradox2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfilePictureIntegrationTest {

    private static final String S3_PREFIX = "http://s3.test.local/test-bucket/";
    private static final String DOWNLOAD_DISPOSITION = "response-content-disposition=inline; filename=\"profile\"";

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(950);

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void uploadProfilePictureShouldPersistKeyAndReturnSignedUrl() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        MvcResult result = mockMvc.perform(putMultipart("/users/me/profile-picture", token, "avatar.png", "image/png",
                        createPng(300, 200)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty())
                .andReturn();

        String url = Json.readUrl(result, objectMapper);

        assertEquals(true, url.startsWith(S3_PREFIX));
        assertEquals(true, url.contains("X-Amz-Signature=test-signature"));
        assertEquals(true, url.contains(DOWNLOAD_DISPOSITION));

        User persisted = userRepository.findByUsername("pic").orElseThrow();
        assertNotNull(persisted.getProfilePictureKey());
        assertEquals(true, url.contains("/" + persisted.getProfilePictureKey() + "?"));
    }

    @Test
    void uploadedPictureShouldBeVisibleInMyProfile() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "avatar.png", "image/png",
                        createPng(300, 200)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());
    }

    @Test
    void storedPictureShouldBeWebpAndSquare() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "avatar.png", "image/png",
                        createPng(400, 200)))
                .andExpect(status().isOk());

        String key = userRepository.findByUsername("pic").orElseThrow().getProfilePictureKey();
        byte[] stored = readObject(key);

        assertEquals("RIFF", new String(stored, 0, 4, StandardCharsets.US_ASCII));
        assertEquals("WEBP", new String(stored, 8, 4, StandardCharsets.US_ASCII));

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(stored));
        assertNotNull(decoded);
        assertEquals(256, decoded.getWidth());
        assertEquals(256, decoded.getHeight());
    }

    @Test
    void uploadingNonImageShouldBeRejected() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "notas.pdf", MediaType.APPLICATION_PDF_VALUE,
                        "pdf-bytes".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        User persisted = userRepository.findByUsername("pic").orElseThrow();
        assertNull(persisted.getProfilePictureKey());
    }

    @Test
    void uploadingOversizedPictureShouldBeRejected() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        byte[] oversized = new byte[6 * 1024 * 1024];
        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "big.png", "image/png", oversized))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        User persisted = userRepository.findByUsername("pic").orElseThrow();
        assertNull(persisted.getProfilePictureKey());
    }

    @Test
    void replacingPictureShouldDeleteThePreviousObject() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "one.png", "image/png",
                        createPng(300, 300)))
                .andExpect(status().isOk());
        String firstKey = userRepository.findByUsername("pic").orElseThrow().getProfilePictureKey();

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "two.png", "image/png",
                        createPng(300, 300)))
                .andExpect(status().isOk());
        String secondKey = userRepository.findByUsername("pic").orElseThrow().getProfilePictureKey();

        assertNotEquals(firstKey, secondKey);
        assertObjectMissing(firstKey);
        assertObjectPresent(secondKey);
    }

    @Test
    void deletingPictureShouldClearKeyAndDeleteObject() throws Exception {
        createEnabledUser("pic", "pic@rai.test", "SecurePass1!");
        String token = loginAndGetToken("pic", "SecurePass1!");

        mockMvc.perform(putMultipart("/users/me/profile-picture", token, "avatar.png", "image/png",
                        createPng(300, 200)))
                .andExpect(status().isOk());
        String key = userRepository.findByUsername("pic").orElseThrow().getProfilePictureKey();

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/me/profile-picture")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());

        User persisted = userRepository.findByUsername("pic").orElseThrow();
        assertNull(persisted.getProfilePictureKey());
        assertObjectMissing(key);
    }

    @Test
    void deletingPictureWithoutAuthenticationShouldBeRejected() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/me/profile-picture"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadingPictureWithoutAuthenticationShouldBeRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png",
                "png-bytes".getBytes());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/users/me/profile-picture")
                .file(file);
        request.with(r -> {
            r.setMethod(HttpMethod.PUT.name());
            return r;
        });
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder putMultipart(String url, String token, String filename, String contentType,
            byte[] payload) {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, payload);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(url)
                .file(file)
                .header("Authorization", bearer(token));
        request.with(r -> {
            r.setMethod(HttpMethod.PUT.name());
            return r;
        });
        return request;
    }

    private byte[] createPng(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private byte[] readObject(String key) throws Exception {
        try (var response = s3Client.getObject(
                GetObjectRequest.builder().bucket("test-bucket").key(key).build())) {
            return response.readAllBytes();
        }
    }

    private void assertObjectPresent(String key) {
        try {
            s3Client.getObject(GetObjectRequest.builder().bucket("test-bucket").key(key).build()).close();
        } catch (Exception e) {
            throw new AssertionError("Se esperaba que el objeto " + key + " existiera en S3", e);
        }
    }

    private void assertObjectMissing(String key) {
        try {
            s3Client.getObject(GetObjectRequest.builder().bucket("test-bucket").key(key).build()).close();
            throw new AssertionError("Se esperaba que el objeto " + key + " hubiera sido borrado de S3");
        } catch (NoSuchKeyException expected) {
            // correcto: el objeto ya no existe
        } catch (Exception e) {
            throw new AssertionError("Fallo inesperado comprobando el borrado de " + key, e);
        }
    }

    private User createEnabledUser(String username, String email, String password) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private static final class Json {
        private static String readUrl(MvcResult result, ObjectMapper objectMapper) throws Exception {
            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.get("profilePictureUrl").asText();
        }
    }
}