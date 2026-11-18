package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Descarga del contenido de una propuesta pendiente (WAVE1 S1).
 *
 * <p>El archivo en revisión todavía no es público: solo lo ve quien pertenece al grado del recurso y
 * puede verlo según su visibilidad. El estado {@code PENDING} por sí solo no autoriza nada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProposalDownloadScopeIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger(9200);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DegreeRepository degreeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

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
    void userFromAnotherDegreeCannotDownloadPendingProposal() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        createUser("uploader", informatica, UserRole.USER);
        createUser("intruder", quimica, UserRole.USER);
        Subject subject = createSubject(informatica, "G4012101");

        long proposalId = uploadProposal("uploader", subject.getId(), "PUBLIC");

        mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(loginAndGetToken("intruder"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"))
                .andExpect(jsonPath("$.url").doesNotExist());
    }

    @Test
    void sameDegreeUserCanDownloadPendingProposalToReviewIt() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        createUser("uploader", informatica, UserRole.USER);
        createUser("reviewer", informatica, UserRole.MASTER);
        Subject subject = createSubject(informatica, "G4012102");

        long proposalId = uploadProposal("uploader", subject.getId(), "PUBLIC");

        mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(loginAndGetToken("reviewer"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void privateProposalIsHiddenFromSameDegreePeers() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        createUser("uploader", informatica, UserRole.USER);
        createUser("peer", informatica, UserRole.USER);
        Subject subject = createSubject(informatica, "G4012103");

        long proposalId = uploadProposal("uploader", subject.getId(), "PRIVATE");

        // No revelar la existencia: 404, no 403.
        mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(loginAndGetToken("peer"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.url").doesNotExist());

        // El propio proponente sí puede recuperar lo que subió.
        mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(loginAndGetToken("uploader"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void platformAdminKeepsOversightAccessAcrossDegrees() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        createUser("uploader", informatica, UserRole.USER);
        createUser("adminuser", quimica, UserRole.ADMIN);
        Subject subject = createSubject(informatica, "G4012104");

        long proposalId = uploadProposal("uploader", subject.getId(), "PUBLIC");

        mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(loginAndGetToken("adminuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private long uploadProposal(String tokenOwner, long subjectId, String visibility) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apuntes.pdf",
                MediaType.APPLICATION_PDF_VALUE, "contenido".getBytes());
        MvcResult result = mockMvc.perform(multipart("/uploadProposal/upload")
                        .file(file)
                        .param("title", "Apuntes")
                        .param("description", "Descripción")
                        .param("type", FileType.APUNTES.name())
                        .param("subjectId", String.valueOf(subjectId))
                        .param("visibilityLevel", visibility)
                        .header("Authorization", bearer(loginAndGetToken(tokenOwner))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private User createUser(String username, Degree degree, UserRole role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@rai.usc.es")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(role)
                .degree(degree)
                .enabled(true)
                .build());
    }

    private Subject createSubject(Degree degree, String code) {
        Course course = courseRepository.findAll().stream()
                .filter(existing -> existing.getDegree().getId().equals(degree.getId())
                        && "1".equals(existing.getCode()))
                .findFirst()
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("1")
                        .name("Primer curso")
                        .degree(degree)
                        .build()));
        return subjectRepository.save(Subject.builder()
                .code(code)
                .name("Asignatura " + code)
                .course(course)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        String ip = "10.20.30." + IP_COUNTER.incrementAndGet();
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .header("X-Forwarded-For", ip)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
