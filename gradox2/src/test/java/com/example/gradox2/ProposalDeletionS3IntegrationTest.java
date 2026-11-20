package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * El borrado en S3 sigue al commit de la base (WAVE1 S8).
 *
 * <p>Un objeto S3 no vuelve con el rollback: si el borrado ocurre dentro de la transacción, un error
 * posterior deja la fila viva apuntando a un archivo que ya no existe. El test fuerza un rollback real y
 * verifica que el objeto y la fila sigan ahí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProposalDeletionS3IntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger(9900);

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
    private TempFileRepository tempFileRepository;

    @Autowired
    private FileProposalRepository fileProposalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryRateLimiter rateLimiter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String token;
    private Long proposalId;
    private String objectKey;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiter.clear();
        databaseCleaner.clean();

        Degree degree = TestDegrees.informatica(degreeRepository);
        createUser("deleter", degree);
        Subject subject = createSubject(degree, "G4012301");
        token = loginAndGetToken("deleter");

        MockMultipartFile file = new MockMultipartFile("file", "apuntes.pdf",
                MediaType.APPLICATION_PDF_VALUE, "contenido".getBytes());
        MvcResult uploaded = mockMvc.perform(multipart("/uploadProposal/upload")
                        .file(file)
                        .param("title", "Apuntes")
                        .param("description", "Descripción")
                        .param("type", FileType.APUNTES.name())
                        .param("subjectId", String.valueOf(subject.getId()))
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn();

        proposalId = objectMapper.readTree(uploaded.getResponse().getContentAsString()).get("id").asLong();
        objectKey = tempFileRepository.findAll().get(0).getObjectKey();
        assertThat(objectExists()).as("el objeto se subió a S3").isTrue();
    }

    @Test
    void deletingAPendingProposalRemovesTheObject() throws Exception {
        mockMvc.perform(delete("/uploadProposal/{id}", proposalId)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk());

        assertThat(objectExists()).as("el objeto se borra cuando la transacción confirma").isFalse();
        assertThat(fileProposalRepository.findById(proposalId)).isEmpty();
    }

    @Test
    void rollbackKeepsBothTheRowAndTheObject() throws Exception {
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        template.execute(status -> {
            try {
                mockMvc.perform(delete("/uploadProposal/{id}", proposalId)
                        .header("Authorization", bearer()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            // Simula un fallo posterior dentro de la misma transacción.
            status.setRollbackOnly();
            return null;
        });

        assertThat(fileProposalRepository.findById(proposalId))
                .as("la fila vuelve con el rollback")
                .isPresent();
        assertThat(objectExists())
                .as("el objeto en S3 no puede desaparecer si la base revierte")
                .isTrue();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private boolean objectExists() {
        try {
            var object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket("test-bucket")
                    .key(objectKey)
                    .build());
            object.close();
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private User createUser(String username, Degree degree) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@rai.usc.es")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(UserRole.USER)
                .degree(degree)
                .enabled(true)
                .build());
    }

    private Subject createSubject(Degree degree, String code) {
        Course course = courseRepository.save(Course.builder()
                .code("3")
                .name("Tercer curso")
                .degree(degree)
                .build());
        return subjectRepository.save(Subject.builder()
                .code(code)
                .name("Asignatura " + code)
                .course(course)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        String ip = "10.80.0." + IP_COUNTER.incrementAndGet();
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

    private String bearer() {
        return "Bearer " + token;
    }
}
