package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VerificationToken;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ForumThreadRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.ThreadCommentRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.persistence.repository.VoteRepository;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.utils.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalJourneyIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(5000);

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
    private VoteRepository voteRepository;

    @Autowired
    private VoteConfigRepository voteConfigRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private FileProposalRepository fileProposalRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TempFileRepository tempFileRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ForumThreadRepository forumThreadRepository;

    @Autowired
    private ThreadCommentRepository threadCommentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IGlobalConfigService voteConfigService;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        threadCommentRepository.deleteAll();
        forumThreadRepository.deleteAll();
        scoreRepository.deleteAll();
        voteRepository.deleteAll();
        fileProposalRepository.deleteAll();
        proposalRepository.deleteAll();
        fileRepository.deleteAll();
        tempFileRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        voteConfigRepository.deleteAll();
        subjectRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);
    }

    @Test
    void fullUserJourneyFromRegistrationToLogout() throws Exception {
        // 1. Registro con correo institucional -> usuario pendiente de verificación.
        MvcResult register = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "journeyuser",
                                "email", "journey@rai.usc.es",
                                "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("journeyuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode regBody = objectMapper.readTree(register.getResponse().getContentAsString());
        JsonNode accessTokenNode = regBody.get("token");
        String accessToken = accessTokenNode != null && !accessTokenNode.isNull() ? accessTokenNode.asText() : null;
        org.junit.jupiter.api.Assertions.assertNull(accessToken);

        User pending = userRepository.findByUsername("journeyuser").orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(pending.isEnabled());

        VerificationToken vt = verificationTokenRepository.findAll().stream().findFirst().orElseThrow();

        // 2. Verificación del correo (mismo flow de la batería).
        mockMvc.perform(get("/api/auth/verify").param("token", vt.getToken()))
                .andExpect(status().isOk());

        // 3. Login del usuario recién verificado.
        JsonNode login1 = login("journeyuser", "SecurePass1!");

        // 4. Perfil propio.
        mockMvc.perform(get("/users/me")
                        .header("Authorization", bearer(login1.get("token").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("journeyuser"));

        // 5. Rename del propio perfil -> vuelve a poder hacer login con el nuevo alias.
        mockMvc.perform(put("/users/me")
                        .header("Authorization", bearer(login1.get("token").asText()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "journeyrenamed", "password", "SecretPass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("journeyrenamed"));

        JsonNode loginRenamed = login("journeyrenamed", "SecretPass1!");
        String token = loginRenamed.get("token").asText();

        // 6. Subida de propuesta (usuario normal).
        Long subjectId = createSubject();
        long proposalId = uploadProposalAndGetId(token, subjectId,
                "temaBateria.pdf", "Tema bateria", "Apuntes del tema");

        // 7. Un MASTER aprueba la propuesta con un voto (quorum=1) -> el archivo se publica.
        String masterToken = createAndGetMaster();

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFavor").value(true));

        MvcResult filesResult = mockMvc.perform(get("/files/all")
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode files = objectMapper.readTree(filesResult.getResponse().getContentAsString());
        long fileId = files.get(0).get("id").asLong();
        String fileName = files.get(0).get("fileName").asText();

        org.junit.jupiter.api.Assertions.assertEquals("Tema bateria", fileName);

        // 8. Detalle y descarga del archivo publicado.
        mockMvc.perform(get("/files/{id}", fileId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("Tema bateria"));

        mockMvc.perform(get("/files/{id}/download", fileId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 9. Comentario: crea el hilo implícitamente; edición propia; otro usuario no puede editar.
        MvcResult comment = mockMvc.perform(post("/files/{id}/comments", fileId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Primer comentario"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorUsername").value("journeyrenamed"))
                .andReturn();

        long commentId = objectMapper.readTree(comment.getResponse().getContentAsString()).get("id").asLong();

        String exToken = createOtherToken();
        mockMvc.perform(put("/files/{id}/comments/{commentId}", fileId, commentId)
                        .header("Authorization", bearer(exToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Intrusión"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        // 10. Bloqueo de hilo por el uploader -> responder en hilo bloqueado da error; unlock permite.
        mockMvc.perform(post("/files/{id}/thread/lock", fileId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/files/{id}/comments", fileId)
                        .header("Authorization", bearer(exToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Hilo bloqueado"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        mockMvc.perform(delete("/files/{id}/thread/lock", fileId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 11. Votación del archivo publica: a favor, cambio de sentido, retirar.
        mockMvc.perform(post("/files/{id}/vote/{upvote}", fileId, true)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFavor").value(true));

        mockMvc.perform(post("/files/{id}/vote/{upvote}", fileId, false)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFavor").value(false));

        mockMvc.perform(delete("/files/{id}/vote", fileId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 12. Cambio de visibilidad como uploader -> un tercero ve "anonymous".
        mockMvc.perform(put("/files/{id}/visibility", fileId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"RESTRICTED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibilityLevel").value("RESTRICTED"));

        mockMvc.perform(get("/files/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploaderUsername").value("anonymous"));

        // 13. Perfil público del usuario renombrado.
        Long renamedId = userRepository.findByUsername("journeyrenamed").orElseThrow().getId();
        mockMvc.perform(get("/users/{id}", renamedId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("journeyrenamed"));

        // 14. Solicitud de promoción del usuario normal, listada por el MASTER y cancelada.
        mockMvc.perform(post("/promoteProposal/request")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/promoteProposal/pending")
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(delete("/promoteProposal/delete")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 15. Vote-config: USER -> 403, MASTER -> 200.
        mockMvc.perform(get("/vote-config")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/vote-config")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "quorumRequired", 1,
                                "approvalThreshold", 0.5,
                                "maxPendingUploads", 3))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/vote-config")
                        .header("Authorization", bearer(masterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "quorumRequired", 1,
                                "approvalThreshold", 0.5,
                                "maxPendingUploads", 3))))
                .andExpect(status().isOk());

        // 16. Refresh con rotación; el refresh antiguo queda invalidado al reutilizarse.
        String oldRefresh = login1.get("refreshToken").asText();

        MvcResult refreshed = mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshedBody = objectMapper.readTree(refreshed.getResponse().getContentAsString());
        String newRefresh = refreshedBody.get("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(oldRefresh, newRefresh);

        // 17. Logout revoca la cadena refresh -> reuso de oldRefresh devuelve 401.
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));

        // 18. Admin: banear y desbanear al usuario normal.
        mockMvc.perform(put("/admin/users/{id}/ban", renamedId)
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/users/{id}/unban", renamedId)
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk());
    }

    private String createUser(String username, String email, String password, UserRole role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .build()).getUsername();
    }

    private String createAndGetMaster() throws Exception {
        createUser("globalmaster", "globalmaster@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        return login("globalmaster", "SecurePass1!").get("token").asText();
    }

    private String createUserAndGetToken(String username, String email, String password) throws Exception {
        createUser(username, email, password, UserRole.USER);
        return login(username, password).get("token").asText();
    }

    private String getOtherUsername() {
        return "other";
    }

    private String createOtherToken() throws Exception {
        createUser(getOtherUsername(), "other@rai.usc.es", "SecurePass1!", UserRole.USER);
        return login(getOtherUsername(), "SecurePass1!").get("token").asText();
    }

    private JsonNode login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr(nextTestIp());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Long createSubject() {
        Course course = courseRepository.save(Course.builder()
                .code("GJ" + System.nanoTime())
                .name("Curso Global Journey")
                .build());
        Subject subject = subjectRepository.save(Subject.builder()
                .code("GS" + System.nanoTime())
                .name("Asignatura Global Journey")
                .course(course)
                .build());
        return subject.getId();
    }

    private long uploadProposalAndGetId(String token, Long subjectId, String originalFilename,
            String title, String description) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                MediaType.APPLICATION_PDF_VALUE,
                "contenido-de-prueba-global".getBytes());

        MockHttpServletRequestBuilder request = multipart("/uploadProposal/upload")
                .file(multipartFile)
                .param("title", title)
                .param("description", description)
                .param("type", FileType.APUNTES.name())
                .param("subjectId", String.valueOf(subjectId))
                .param("visibilityLevel", FileVisibility.PUBLIC.name())
                .header("Authorization", bearer(token));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String nextTestIp() {
        return "198.51.100." + IP_COUNTER.incrementAndGet();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}