package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
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
class AdminAndFileDeletionIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(700);

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
    void masterShouldBeAbleToBanAndUnbanAUser() throws Exception {
        createEnabledUser("masteradmin", "masteradmin@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        createEnabledUser("candidate", "candidate@rai.usc.es", "SecurePass1!", UserRole.USER);

        String masterToken = loginAndGetToken("masteradmin", "SecurePass1!");

        mockMvc.perform(put("/admin/users/{id}/ban", candidateId())
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Usuario baneado correctamente"));

        org.junit.jupiter.api.Assertions.assertFalse(userRepository.findByUsername("candidate").orElseThrow().isEnabled());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "candidate", "password", "SecurePass1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED_ACCESS"));

        mockMvc.perform(put("/admin/users/{id}/unban", candidateId())
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Usuario rehabilitado correctamente"));

        org.junit.jupiter.api.Assertions.assertTrue(userRepository.findByUsername("candidate").orElseThrow().isEnabled());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "candidate", "password", "SecurePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void nonMasterShouldNotBeAbleToBanUsers() throws Exception {
        createEnabledUser("regular", "regular@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("target", "target@rai.usc.es", "SecurePass1!", UserRole.USER);

        String userToken = loginAndGetToken("regular", "SecurePass1!");

        mockMvc.perform(put("/admin/users/{id}/ban", targetId())
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fileDeletionProposalShouldExposeMetadataAndRemainPendingBelowQuorum() throws Exception {
        setupPublishedFile();
        File publishedFile = fileRepository.findAll().stream().findFirst().orElseThrow();

        String requesterToken = loginAndGetToken("deletionProposer", "SecurePass1!");
        String voterToken = loginAndGetToken("deletionVoter", "SecurePass1!");

        MvcResult result = mockMvc.perform(delete("/files/{id}", publishedFile.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tema publicado"))
                .andExpect(jsonPath("$.description").value("Descripcion publicada"))
                .andExpect(jsonPath("$.proposer").value("deletionProposer"))
                .andExpect(jsonPath("$.subjectName").value("Asignatura Test"))
                .andExpect(jsonPath("$.course").value("Curso Test"))
                .andExpect(jsonPath("$.type").value(FileType.APUNTES.name()))
                .andExpect(jsonPath("$.status").value(ProposalStatus.PENDING.name()))
                .andReturn();

        long deleteProposalId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vote/{id}/{upvote}", deleteProposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

        FileProposal pendingDelete = fileProposalRepository.findById(deleteProposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.PENDING, pendingDelete.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(fileRepository.findById(publishedFile.getId()).isPresent());
    }

    @Test
    void fileDeletionProposalShouldApproveAndRemoveFileWhenQuorumIsMet() throws Exception {
        setupPublishedFile();
        File publishedFile = fileRepository.findAll().stream().findFirst().orElseThrow();

        String requesterToken = loginAndGetToken("deletionProposer", "SecurePass1!");
        String voterToken = loginAndGetToken("deletionVoter", "SecurePass1!");

        MvcResult result = mockMvc.perform(delete("/files/{id}", publishedFile.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isOk())
                .andReturn();

        long deleteProposalId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/vote/{id}/{upvote}", deleteProposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", deleteProposalId, true)
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isOk());

        FileProposal approvedDelete = fileProposalRepository.findById(deleteProposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.APPROVED, approvedDelete.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(fileRepository.findById(publishedFile.getId()).isEmpty());
    }

    @Test
    void duplicateFileDeletionProposalShouldBeRejectedWhilePending() throws Exception {
        setupPublishedFile();
        File publishedFile = fileRepository.findAll().stream().findFirst().orElseThrow();

        String requesterToken = loginAndGetToken("deletionProposer", "SecurePass1!");

        mockMvc.perform(delete("/files/{id}", publishedFile.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/files/{id}", publishedFile.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));
    }

    private void setupPublishedFile() throws Exception {
        voteConfigRepository.save(VoteConfig.builder().quorumRequired(2).approvalThreshold(0.5).build());
        Long subjectId = createSubject();

        createEnabledUser("deletionProposer", "deletionProposer@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("deletionVoter", "deletionVoter@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("deletionProposer", "SecurePass1!");
        String voterToken = loginAndGetToken("deletionVoter", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "tema-publicado.pdf", "Tema publicado", "Descripcion publicada");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(fileRepository.findAll().isEmpty());
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

    private Long candidateId() {
        return userRepository.findByUsername("candidate").orElseThrow().getId();
    }

    private Long targetId() {
        return userRepository.findByUsername("target").orElseThrow().getId();
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

        MockHttpServletRequestBuilder request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/uploadProposal/upload")
                .file(multipartFile)
                .param("title", title)
                .param("description", description)
                .param("type", FileType.APUNTES.name())
                .param("subjectId", String.valueOf(subjectId))
                .header("Authorization", bearer(token));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll("[^0-9]", ""));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String nextTestIp() {
        return "203.0.113." + IP_COUNTER.incrementAndGet();
    }
}
