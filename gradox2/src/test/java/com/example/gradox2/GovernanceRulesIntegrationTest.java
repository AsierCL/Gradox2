package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.entities.enums.ActionType;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GovernanceRulesIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(100);

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
    }

    @Test
    void fileProposalShouldRemainPendingBelowQuorum() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(3).approvalThreshold(0.67).build());

        Long subjectId = createSubject();
        createEnabledUser("proposerA", "proposerA@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterA1", "voterA1@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterA2", "voterA2@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("proposerA", "SecurePass1!");
        String voter1 = loginAndGetToken("voterA1", "SecurePass1!");
        String voter2 = loginAndGetToken("voterA2", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaA.pdf", "Tema A", "Desc A");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/uploadProposal/{id}", proposalId).header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/files/all").header("Authorization", bearer(voter1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
        void fileProposalShouldBeRejectedWhenApprovalRatioIsInsufficientAfterQuorum() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(3).approvalThreshold(0.67).build());

        Long subjectId = createSubject();
        createEnabledUser("proposerB", "proposerB@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterB1", "voterB1@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterB2", "voterB2@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterB3", "voterB3@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("proposerB", "SecurePass1!");
        String voter1 = loginAndGetToken("voterB1", "SecurePass1!");
        String voter2 = loginAndGetToken("voterB2", "SecurePass1!");
        String voter3 = loginAndGetToken("voterB3", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaB.pdf", "Tema B", "Desc B");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, false).header("Authorization", bearer(voter2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, false).header("Authorization", bearer(voter3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/uploadProposal/{id}", proposalId).header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(get("/files/all").header("Authorization", bearer(voter1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void fileProposalShouldApproveWhenQuorumAndThresholdAreMet() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(3).approvalThreshold(0.67).build());

        Long subjectId = createSubject();
        createEnabledUser("proposerC", "proposerC@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterC1", "voterC1@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterC2", "voterC2@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterC3", "voterC3@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("proposerC", "SecurePass1!");
        String voter1 = loginAndGetToken("voterC1", "SecurePass1!");
        String voter2 = loginAndGetToken("voterC2", "SecurePass1!");
        String voter3 = loginAndGetToken("voterC3", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaC.pdf", "Tema C", "Desc C");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/uploadProposal/{id}", proposalId).header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/files/all").header("Authorization", bearer(voter1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("Tema C"));
    }

    @Test
    void proposalShouldKeepSnapshotOfVoteConfigAtCreationTime() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(2).approvalThreshold(0.5).build());

        Long subjectId = createSubject();
        createEnabledUser("proposerD", "proposerD@rai.usc.es", "SecurePass1!", UserRole.USER);
        String proposerToken = loginAndGetToken("proposerD", "SecurePass1!");

        long firstProposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaD1.pdf", "Tema D1", "Desc D1");

        voteConfigRepository.deleteAll();
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(5).approvalThreshold(0.9).build());

        long secondProposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaD2.pdf", "Tema D2", "Desc D2");

        Proposal first = proposalRepository.findById(firstProposalId).orElseThrow();
        Proposal second = proposalRepository.findById(secondProposalId).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(2, first.getQuorumRequired());
        org.junit.jupiter.api.Assertions.assertEquals(0.5, first.getApprovalThreshold());
        org.junit.jupiter.api.Assertions.assertEquals(5, second.getQuorumRequired());
        org.junit.jupiter.api.Assertions.assertEquals(0.9, second.getApprovalThreshold());
    }

    @Test
    void uploadProposalShouldCreateDefaultVoteConfigWhenMissing() throws Exception {
        Long subjectId = createSubject();
        createEnabledUser("proposerE", "proposerE@rai.usc.es", "SecurePass1!", UserRole.USER);
        String proposerToken = loginAndGetToken("proposerE", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaE.pdf", "Tema E", "Desc E");

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(5, proposal.getQuorumRequired());
        org.junit.jupiter.api.Assertions.assertEquals(0.6, proposal.getApprovalThreshold());
    }

    @Test
    void demoteShouldCreateExpulsionProposalAndApplyRoleDowngradeWhenApproved() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(2).approvalThreshold(0.5).build());

        User candidate = createEnabledUser("masterCandidate", "masterCandidate@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        createEnabledUser("requester", "requester@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("demoter1", "demoter1@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("demoter2", "demoter2@rai.usc.es", "SecurePass1!", UserRole.USER);

        String requesterToken = loginAndGetToken("requester", "SecurePass1!");
        String voter1 = loginAndGetToken("demoter1", "SecurePass1!");
        String voter2 = loginAndGetToken("demoter2", "SecurePass1!");

        MvcResult demoteResult = mockMvc.perform(post("/promoteProposal/demote/{id}", candidate.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidate").value("masterCandidate"))
                .andReturn();

        long proposalId = objectMapper.readTree(demoteResult.getResponse().getContentAsString()).get("id").asLong();
        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ActionType.EXPULSION, proposal.getActionType());

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter2)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(candidate.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.USER, updated.getRole());
    }

    @Test
    void demoteShouldRejectWhenCandidateIsNotMaster() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(1).approvalThreshold(0.5).build());

        User candidate = createEnabledUser("plainUser", "plainUser@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("requester2", "requester2@rai.usc.es", "SecurePass1!", UserRole.USER);
        String requesterToken = loginAndGetToken("requester2", "SecurePass1!");

        mockMvc.perform(post("/promoteProposal/demote/{id}", candidate.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ROLE_OPERATION"));
    }

    @Test
    void demoteShouldReturnNotFoundWhenCandidateDoesNotExist() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(1).approvalThreshold(0.5).build());

        createEnabledUser("requester3", "requester3@rai.usc.es", "SecurePass1!", UserRole.USER);
        String requesterToken = loginAndGetToken("requester3", "SecurePass1!");

        mockMvc.perform(post("/promoteProposal/demote/{id}", 99999L)
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
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

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                                                .with(request -> {
                                                        request.setRemoteAddr(nextTestIp());
                                                        return request;
                                                })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String nextTestIp() {
        return "203.0.113." + IP_COUNTER.incrementAndGet();
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

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
