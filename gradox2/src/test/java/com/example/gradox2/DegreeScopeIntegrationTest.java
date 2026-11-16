package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ámbito de grado en las escrituras de archivos (GRADOS.md §5.1, §5.2). Cada caso verifica también el
 * efecto negativo: que el rechazo no deje filas creadas y que no consuma cuota.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DegreeScopeIntegrationTest {

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
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TempFileRepository tempFileRepository;

    @Autowired
    private FileProposalRepository fileProposalRepository;

    @Autowired
    private ScoreRepository scoreRepository;

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

    // ── subida de propuestas ────────────────────────────────────────────────

    @Test
    void uploadInOwnDegreeIsAccepted() throws Exception {
        User student = createUser("student", TestDegrees.informatica(degreeRepository), UserRole.USER);
        Subject own = createSubject(student.getDegree(), "G4012101");
        String token = loginAndGetToken("student");

        mockMvc.perform(uploadRequest(token, own.getId(), "Apuntes"))
                .andExpect(status().isOk());

        assertThat(tempFileRepository.count()).isEqualTo(1);
        assertThat(fileProposalRepository.count()).isEqualTo(1);
    }

    @Test
    void uploadToAnotherDegreeSubjectIsForbiddenAndLeavesNothingBehind() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        createUser("student", informatica, UserRole.USER);
        createUser("foreign", quimica, UserRole.USER);
        Subject foreignSubject = createSubject(quimica, "G4022101");
        String token = loginAndGetToken("student");

        mockMvc.perform(uploadRequest(token, foreignSubject.getId(), "Apuntes ajenos"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(tempFileRepository.count()).as("no debe crearse el archivo temporal").isZero();
        assertThat(fileProposalRepository.count()).as("no debe crearse la propuesta").isZero();

        // La cuota no se consumió: la subida en el grado propio sigue funcionando.
        Subject ownSubject = createSubject(informatica, "G4012102");
        mockMvc.perform(uploadRequest(token, ownSubject.getId(), "Apuntes propios"))
                .andExpect(status().isOk());
        assertThat(tempFileRepository.count()).isEqualTo(1);
    }

    // ── puntuación de archivos ──────────────────────────────────────────────

    @Test
    void scoringAFileFromAnotherDegreeIsForbidden() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        User uploader = createUser("owner", informatica, UserRole.USER);
        createUser("foreign", quimica, UserRole.USER);
        File file = createPublishedFile(uploader, createSubject(informatica, "G4012101"));
        String token = loginAndGetToken("foreign");

        mockMvc.perform(post("/files/{id}/vote/{upvote}", file.getId(), true)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(scoreRepository.count()).as("el voto no debe registrarse").isZero();
    }

    @Test
    void scoringAFileFromOwnDegreeIsAccepted() throws Exception {
        User uploader = createUser("owner", TestDegrees.informatica(degreeRepository), UserRole.USER);
        File file = createPublishedFile(uploader, createSubject(uploader.getDegree(), "G4012101"));
        String token = loginAndGetToken("owner");

        mockMvc.perform(post("/files/{id}/vote/{upvote}", file.getId(), true)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        assertThat(scoreRepository.count()).isEqualTo(1);
    }

    // ── propuesta de borrado ────────────────────────────────────────────────

    @Test
    void requestingDeletionOfAFileFromAnotherDegreeIsForbidden() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        User uploader = createUser("owner", informatica, UserRole.USER);
        createUser("foreign", quimica, UserRole.USER);
        File file = createPublishedFile(uploader, createSubject(informatica, "G4012101"));
        String token = loginAndGetToken("foreign");

        mockMvc.perform(delete("/files/{id}", file.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(fileProposalRepository.count()).as("no debe crearse la propuesta de borrado").isZero();
    }

    // ── visibilidad ─────────────────────────────────────────────────────────

    @Test
    void masterOfAnotherDegreeCannotChangeFileVisibility() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        User uploader = createUser("owner", informatica, UserRole.USER);
        createUser("foreignmaster", quimica, UserRole.MASTER);
        File file = createPublishedFile(uploader, createSubject(informatica, "G4012101"));
        String token = loginAndGetToken("foreignmaster");

        mockMvc.perform(put("/files/{id}/visibility", file.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"PRIVATE\""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(fileRepository.findById(file.getId()).orElseThrow().getVisibilityLevel())
                .as("la visibilidad no cambia").isEqualTo(FileVisibility.PUBLIC);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private MockHttpServletRequestBuilder uploadRequest(String token, Long subjectId, String title) {
        MockMultipartFile file = new MockMultipartFile("file", "apuntes.pdf",
                MediaType.APPLICATION_PDF_VALUE, "contenido".getBytes());
        return multipart("/uploadProposal/upload")
                .file(file)
                .param("title", title)
                .param("description", "Descripción")
                .param("type", FileType.APUNTES.name())
                .param("subjectId", String.valueOf(subjectId))
                .header("Authorization", bearer(token));
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
        // (degree_id, code) es único: el curso se reutiliza dentro del mismo grado.
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

    private File createPublishedFile(User uploader, Subject subject) {
        return fileRepository.save(File.builder()
                .title("Apuntes")
                .description("Apuntes de prueba")
                .type(FileType.APUNTES)
                .objectKey("files/" + subject.getCode() + "-" + uploader.getUsername() + ".pdf")
                .originalFilename("apuntes.pdf")
                .sizeBytes(10L)
                .fileHash("hash-" + subject.getCode())
                .subject(subject)
                .uploader(uploader)
                .visibilityLevel(FileVisibility.PUBLIC)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
