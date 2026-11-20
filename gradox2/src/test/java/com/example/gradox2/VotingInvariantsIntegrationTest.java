package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
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
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Invariantes que la base tiene que sostener bajo concurrencia (WAVE1 S6).
 *
 * <p>Los dos chequeos eran "contar y después insertar": dos peticiones simultáneas podían dejar dos
 * puntuaciones del mismo usuario o exceder la cuota de subidas pendientes. La regla no es "una de las dos
 * falla con tal código", es "el estado final nunca viola el límite", así que eso es lo que se verifica.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VotingInvariantsIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger(9800);

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
    private ScoreRepository scoreRepository;

    @Autowired
    private FileProposalRepository fileProposalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRateLimiter rateLimiter;

    @Autowired
    private IGlobalConfigService voteConfigService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        databaseCleaner.clean();
        voteConfigService.reloadConfig();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void concurrentVotesFromTheSameUserLeaveExactlyOneScore() throws Exception {
        Degree degree = TestDegrees.informatica(degreeRepository);
        User voter = createUser("racer", degree, UserRole.USER);
        File file = createFile(createSubject(degree, "G4012201"), voter);
        String token = loginAndGetToken("racer");

        CyclicBarrier barrier = new CyclicBarrier(2);
        List<Integer> statuses = runConcurrently(
                voteCall(barrier, file.getId(), token),
                voteCall(barrier, file.getId(), token));

        assertThat(scoreRepository.count())
                .as("un único score para el par (archivo, votante)")
                .isEqualTo(1);
        assertThat(statuses).allMatch(status -> status == 200 || status == 409);
    }

    @Test
    void concurrentUploadsNeverExceedThePendingQuota() throws Exception {
        voteConfigService.updateConfig(null, null, 1);

        Degree degree = TestDegrees.informatica(degreeRepository);
        User uploader = createUser("uploader", degree, UserRole.USER);
        Subject subject = createSubject(degree, "G4012202");
        String token = loginAndGetToken("uploader");

        CyclicBarrier barrier = new CyclicBarrier(2);
        List<Integer> statuses = runConcurrently(
                uploadCall(barrier, subject.getId(), token, "Primero"),
                uploadCall(barrier, subject.getId(), token, "Segundo"));

        long pending = fileProposalRepository.countByProposerAndStatusAndActionType(
                uploader, ProposalStatus.PENDING, ActionType.UPLOAD);

        assertThat(pending).as("la cuota configurada es 1").isLessThanOrEqualTo(1);
        assertThat(statuses.stream().filter(status -> status == 200).count())
                .as("solo una subida puede entrar en la cuota")
                .isEqualTo(1);
        assertThat(statuses.stream().filter(status -> status == 400).count())
                .as("la otra se rechaza por cuota, no por un 500")
                .isEqualTo(1);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Callable<Integer> voteCall(CyclicBarrier barrier, Long fileId, String token) {
        return () -> {
            barrier.await(15, TimeUnit.SECONDS);
            return mockMvc.perform(post("/files/{id}/vote/{upvote}", fileId, true)
                            .header("Authorization", bearer(token)))
                    .andReturn().getResponse().getStatus();
        };
    }

    private Callable<Integer> uploadCall(CyclicBarrier barrier, Long subjectId, String token, String title) {
        return () -> {
            MockMultipartFile file = new MockMultipartFile("file", "apuntes.pdf",
                    MediaType.APPLICATION_PDF_VALUE, ("contenido " + title).getBytes());
            barrier.await(15, TimeUnit.SECONDS);
            return mockMvc.perform(multipart("/uploadProposal/upload")
                            .file(file)
                            .param("title", title)
                            .param("description", "Descripción")
                            .param("type", FileType.APUNTES.name())
                            .param("subjectId", String.valueOf(subjectId))
                            .header("Authorization", bearer(token)))
                    .andReturn().getResponse().getStatus();
        };
    }

    private List<Integer> runConcurrently(Callable<Integer> first, Callable<Integer> second) throws Exception {
        List<Future<Integer>> futures = executor.invokeAll(List.of(first, second), 60, TimeUnit.SECONDS);
        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> future : futures) {
            assertThat(future.isCancelled()).as("la petición terminó dentro del tiempo").isFalse();
            statuses.add(future.get());
        }
        return statuses;
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
        Course course = courseRepository.save(Course.builder()
                .code("2")
                .name("Segundo curso")
                .degree(degree)
                .build());
        return subjectRepository.save(Subject.builder()
                .code(code)
                .name("Asignatura " + code)
                .course(course)
                .build());
    }

    private File createFile(Subject subject, User uploader) {
        return fileRepository.save(File.builder()
                .title("Apuntes")
                .description("Apuntes de prueba")
                .type(FileType.APUNTES)
                .objectKey("files/" + subject.getCode() + ".pdf")
                .originalFilename("apuntes.pdf")
                .sizeBytes(10L)
                .fileHash("hash-" + subject.getCode())
                .subject(subject)
                .uploader(uploader)
                .visibilityLevel(FileVisibility.PUBLIC)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        String ip = "10.70.0." + IP_COUNTER.incrementAndGet();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
