package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.example.gradox2.persistence.entities.ConfigProposal;
import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VoteRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gobernanza por grado (D3, D4, D11, D12): cada grado vota lo suyo, la configuración de votación la votan
 * los masters de todos los grados y el ADMIN no vota.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DegreeVotingIntegrationTest {

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
    private ProposalRepository proposalRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private PromotionProposalRepository promotionProposalRepository;
    @Autowired
    private IGlobalConfigService voteConfigService;
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
        voteConfigService.reloadConfig();
        // Quórum 2: deja observar el conteo por grado con pocos votantes.
        voteConfigService.updateConfig(2, 0.5, 3);
    }

    // ── elegibilidad ────────────────────────────────────────────────────────

    @Test
    void votingAProposalFromAnotherDegreeIsForbidden() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);

        User owner = createUser("owner", informatica, UserRole.USER);
        createUser("foreign", quimica, UserRole.USER);
        Subject subject = createSubject(informatica, "G4012101");

        long proposalId = uploadProposal(loginAndGetToken("owner"), subject.getId(), owner);
        String foreignToken = loginAndGetToken("foreign");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(foreignToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(voteRepository.count()).as("el voto no debe registrarse").isZero();
        assertThat(proposalRepository.findById(proposalId).orElseThrow().getStatus())
                .as("la propuesta sigue abierta").isEqualTo(ProposalStatus.PENDING);
    }

    @Test
    void adminCannotVoteAnyProposal() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        User owner = createUser("owner", informatica, UserRole.USER);
        createUser("opsuser", informatica, UserRole.ADMIN);
        Subject subject = createSubject(informatica, "G4012101");

        long proposalId = uploadProposal(loginAndGetToken("owner"), subject.getId(), owner);
        String adminToken = loginAndGetToken("opsuser");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ROLE_OPERATION"));

        assertThat(voteRepository.count()).isZero();
    }

    // ── conteo por grado ────────────────────────────────────────────────────

    @Test
    void votesFromOtherDegreesDoNotCountTowardsQuorum() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree ia = TestDegrees.ia(degreeRepository);

        User owner = createUser("owner", informatica, UserRole.USER);
        createUser("informatica2", informatica, UserRole.USER);
        User iaUser = createUser("iauser", ia, UserRole.USER);
        Subject subject = createSubject(informatica, "G4012101");

        long proposalId = uploadProposal(loginAndGetToken("owner"), subject.getId(), owner);
        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();

        // Voto previo al cambio de grado / dato inconsistente: existe, pero no debe contar.
        voteRepository.save(Vote.builder().voter(iaUser).proposal(proposal).inFavor(true).build());

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(loginAndGetToken("owner"))))
                .andExpect(status().isOk());

        assertThat(proposalRepository.findById(proposalId).orElseThrow().getStatus())
                .as("un voto del grado propio no alcanza el quórum de 2").isEqualTo(ProposalStatus.PENDING);

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(loginAndGetToken("informatica2"))))
                .andExpect(status().isOk());

        assertThat(proposalRepository.findById(proposalId).orElseThrow().getStatus())
                .as("con dos votos del grado propio se alcanza el quórum").isEqualTo(ProposalStatus.APPROVED);
    }

    // ── configuración global (D11) ──────────────────────────────────────────

    @Test
    void configProposalIsVotedByMastersOfAnyDegree() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);

        User proposer = createUser("informaster", informatica, UserRole.MASTER);
        createUser("quimicamaster", quimica, UserRole.MASTER);

        ConfigProposal proposal = new ConfigProposal();
        proposal.setProposer(proposer);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.POLICY_CHANGE);
        proposal.setQuorumRequired(2);
        proposal.setApprovalThreshold(0.5);
        proposal.setProposedQuorumRequired(3);
        proposalRepository.save(proposal);

        String quimicaMasterToken = loginAndGetToken("quimicamaster");

        mockMvc.perform(post("/vote/{id}/{upvote}", proposal.getId(), true)
                        .header("Authorization", bearer(quimicaMasterToken)))
                .andExpect(status().isOk());

        assertThat(voteRepository.count()).as("un master de otro grado sí vota la config global").isEqualTo(1);
    }

    @Test
    void nonMasterCannotVoteAGlobalConfigProposal() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        User proposer = createUser("informaster", informatica, UserRole.MASTER);
        createUser("plainuser", informatica, UserRole.USER);

        ConfigProposal proposal = new ConfigProposal();
        proposal.setProposer(proposer);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.POLICY_CHANGE);
        proposal.setQuorumRequired(2);
        proposal.setApprovalThreshold(0.5);
        proposalRepository.save(proposal);

        mockMvc.perform(post("/vote/{id}/{upvote}", proposal.getId(), true)
                        .header("Authorization", bearer(loginAndGetToken("plainuser"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ROLE_OPERATION"));

        assertThat(voteRepository.count()).isZero();
    }

    // ── promociones ─────────────────────────────────────────────────────────

    @Test
    void masterOfAnotherDegreeCannotVoteAPromotionProposal() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);

        User proposer = createUser("informaster", informatica, UserRole.MASTER);
        User candidate = createUser("candidate", informatica, UserRole.USER);
        createUser("quimicamaster", quimica, UserRole.MASTER);

        PromotionProposal proposal = new PromotionProposal();
        proposal.setProposer(proposer);
        proposal.setCandidate(candidate);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.PROMOTION);
        proposal.setQuorumRequired(1);
        proposal.setApprovalThreshold(0.5);
        promotionProposalRepository.save(proposal);

        mockMvc.perform(post("/vote/{id}/{upvote}", proposal.getId(), true)
                        .header("Authorization", bearer(loginAndGetToken("quimicamaster"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("DEGREE_SCOPE_FORBIDDEN"));

        assertThat(voteRepository.count()).isZero();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private long uploadProposal(String token, Long subjectId, User uploader) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "apuntes.pdf",
                MediaType.APPLICATION_PDF_VALUE, "contenido".getBytes());

        MvcResult result = mockMvc.perform(multipart("/uploadProposal/upload")
                        .file(file)
                        .param("title", "Apuntes")
                        .param("description", "Descripción")
                        .param("type", FileType.APUNTES.name())
                        .param("subjectId", String.valueOf(subjectId))
                        .header("Authorization", bearer(token)))
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
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("198.18.0." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
