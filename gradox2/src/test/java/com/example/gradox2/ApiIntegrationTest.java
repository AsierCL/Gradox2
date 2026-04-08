package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.persistence.repository.VoteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(10);

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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
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

        voteConfigRepository.save(VoteConfig.builder()
            .quorumRequired(1)
            .approvalThreshold(0.5)
            .build());
    }

    @Test
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void usersMeShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginShouldReturnTokenForEnabledUser() throws Exception {
        createEnabledUser("aliceuser", "alice@rai.usc.es", "SecurePass1!", UserRole.USER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "aliceuser", "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("aliceuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginShouldFailForWrongPassword() throws Exception {
        createEnabledUser("bobuser", "bob@rai.usc.es", "SecurePass1!", UserRole.USER);

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", nextTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "bobuser", "password", "WrongPass123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));
    }

    @Test
    void loginShouldValidatePayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "ab", "password", "short"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersMeShouldReturnProfileWhenAuthenticated() throws Exception {
        createEnabledUser("charlieuser", "charlie@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetToken("charlieuser", "SecurePass1!");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlieuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void usersAllShouldRejectInvalidPageSize() throws Exception {
        createEnabledUser("dianauser", "diana@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetToken("dianauser", "SecurePass1!");

        ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(get("/users/all")
            .header("Authorization", bearer(token))
            .param("paged", "true")
            .param("page", "0")
            .param("size", "101"))
            .andReturn());

        assertInstanceOf(ConstraintViolationException.class, ex.getCause());
    }

    @Test
    void userProfileShouldRejectNonPositiveId() throws Exception {
        createEnabledUser("eveuser", "eve@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetToken("eveuser", "SecurePass1!");

        ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(get("/users/{id}", 0)
            .header("Authorization", bearer(token)))
            .andReturn());

        assertInstanceOf(ConstraintViolationException.class, ex.getCause());
    }

    @Test
    void voteConfigPutShouldRequireMasterRole() throws Exception {
        createEnabledUser("frankuser", "frank@rai.usc.es", "SecurePass1!", UserRole.USER);
        String userToken = loginAndGetToken("frankuser", "SecurePass1!");

        Map<String, Object> body = new HashMap<>();
        body.put("quorumRequired", 8);
        body.put("approvalThreshold", 0.75);

        mockMvc.perform(put("/vote-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearer(userToken))
                        .content(json(body)))
                .andExpect(status().isForbidden());
    }

            @Test
            void uploadProposalApproveAndPublishFileShouldWorkEndToEnd() throws Exception {
            Long subjectId = createSubject();

            createEnabledUser("proposer1", "proposer1@rai.usc.es", "SecurePass1!", UserRole.USER);
            createEnabledUser("voter1", "voter1@rai.usc.es", "SecurePass1!", UserRole.USER);

            String proposerToken = loginAndGetToken("proposer1", "SecurePass1!");
            String voterToken = loginAndGetToken("voter1", "SecurePass1!");

            long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "tema1.pdf", "Tema 1", "Apuntes tema 1");

            mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                    .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk());

            mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposalId").value(proposalId))
                .andExpect(jsonPath("$.inFavor").value(true));

            mockMvc.perform(get("/vote/{id}/results", proposalId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1))
                .andExpect(jsonPath("$.downvotes").value(0));

            MvcResult filesResult = mockMvc.perform(get("/files/all")
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("Tema 1"))
                .andReturn();

            JsonNode files = objectMapper.readTree(filesResult.getResponse().getContentAsString());
            long fileId = files.get(0).get("id").asLong();

            mockMvc.perform(get("/files/{id}", fileId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("Tema 1"));

            mockMvc.perform(get("/files/{id}/download", fileId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/uploadProposal/{id}", proposalId)
                    .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PROPOSAL_CLOSED"));
            }

            @Test
            void pendingProposalShouldAllowDownvoteRetractAndDeleteByOwner() throws Exception {
            Long subjectId = createSubject();

            createEnabledUser("proposer2", "proposer2@rai.usc.es", "SecurePass1!", UserRole.USER);
            createEnabledUser("voter2", "voter2@rai.usc.es", "SecurePass1!", UserRole.USER);

            String proposerToken = loginAndGetToken("proposer2", "SecurePass1!");
            String voterToken = loginAndGetToken("voter2", "SecurePass1!");

            long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "tema2.pdf", "Tema 2", "Apuntes tema 2");

            mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, false)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFavor").value(false));

            mockMvc.perform(get("/vote/{id}/results", proposalId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(0))
                .andExpect(jsonPath("$.downvotes").value(1));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/vote/{id}", proposalId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

            mockMvc.perform(get("/vote/{id}/results", proposalId)
                    .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(0))
                .andExpect(jsonPath("$.downvotes").value(0));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/uploadProposal/{id}", proposalId)
                    .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk());

            mockMvc.perform(get("/uploadProposal/{id}", proposalId)
                    .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
            }

            @Test
            void fileVoteShouldAllowChangeAndRetractFlow() throws Exception {
            Long subjectId = createSubject();

            createEnabledUser("proposer3", "proposer3@rai.usc.es", "SecurePass1!", UserRole.USER);
            createEnabledUser("approver3", "approver3@rai.usc.es", "SecurePass1!", UserRole.USER);
            createEnabledUser("scorer3", "scorer3@rai.usc.es", "SecurePass1!", UserRole.USER);

            String proposerToken = loginAndGetToken("proposer3", "SecurePass1!");
            String approverToken = loginAndGetToken("approver3", "SecurePass1!");
            String scorerToken = loginAndGetToken("scorer3", "SecurePass1!");

            long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "tema3.pdf", "Tema 3", "Apuntes tema 3");

            mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                    .header("Authorization", bearer(approverToken)))
                .andExpect(status().isOk());

            MvcResult filesResult = mockMvc.perform(get("/files/all")
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk())
                .andReturn();

            JsonNode files = objectMapper.readTree(filesResult.getResponse().getContentAsString());
            long fileId = files.get(0).get("id").asLong();

            mockMvc.perform(post("/files/{id}/vote/{upvote}", fileId, true)
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposalId").value(fileId))
                .andExpect(jsonPath("$.inFavor").value(true));

            mockMvc.perform(post("/files/{id}/vote/{upvote}", fileId, false)
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFavor").value(false));

            mockMvc.perform(get("/files/{id}", fileId)
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(-1.0));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/files/{id}/vote", fileId)
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk());

            mockMvc.perform(get("/files/{id}", fileId)
                    .header("Authorization", bearer(scorerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(0.0));
            }

    private void createEnabledUser(String username, String email, String password, UserRole role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .build();
        userRepository.save(user);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", nextTestIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String nextTestIp() {
        return "198.51.100." + IP_COUNTER.incrementAndGet();
    }

        private Long createSubject() {
        Course course = courseRepository.save(Course.builder()
            .code("C" + System.nanoTime())
            .name("Curso Test")
            .build());

        Subject subject = subjectRepository.save(Subject.builder()
            .code("S" + System.nanoTime())
            .name("Asignatura Test")
            .course(course)
            .build());

        return subject.getId();
        }

        private long uploadProposalAndGetId(String token, Long subjectId, String originalFilename, String title, String description)
            throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            originalFilename,
            MediaType.APPLICATION_PDF_VALUE,
            "contenido-de-prueba".getBytes());

        MockHttpServletRequestBuilder request = multipart("/uploadProposal/upload")
            .file(multipartFile)
            .param("title", title)
            .param("description", description)
            .param("type", FileType.APUNTES.name())
            .param("subjectId", String.valueOf(subjectId))
            .header("Authorization", bearer(token));

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
        }
}
