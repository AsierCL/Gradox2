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
import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.persistence.repository.VoteRepository;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.service.interfaces.IVoteService;
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

        @Autowired
        private IGlobalConfigService voteConfigService;

    @Autowired
    private IVoteService voteService;

    @Autowired
    private PromotionProposalRepository promotionProposalRepository;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        voteRepository.deleteAll();
        promotionProposalRepository.deleteAll();
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
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(3, 0.67, 3);

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
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(3, 0.67, 3);

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
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(3, 0.67, 3);

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
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(2, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("proposerD", "proposerD@rai.usc.es", "SecurePass1!", UserRole.USER);
        String proposerToken = loginAndGetToken("proposerD", "SecurePass1!");

        long firstProposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaD1.pdf", "Tema D1", "Desc D1");

        voteConfigRepository.deleteAll();
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(5, 0.9, 3);

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
                voteConfigService.reloadConfig();

        Long subjectId = createSubject();
        createEnabledUser("proposerE", "proposerE@rai.usc.es", "SecurePass1!", UserRole.USER);
        String proposerToken = loginAndGetToken("proposerE", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaE.pdf", "Tema E", "Desc E");

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(5, proposal.getQuorumRequired());
        org.junit.jupiter.api.Assertions.assertEquals(0.6, proposal.getApprovalThreshold());
    }

    @Test
    void fileProposalShouldSnapshotVoteWeightsOnFirstVoteAndUseThemForStatus() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(2, 0.5, 3, 2.0, 1.0);

        Long subjectId = createSubject();
        createEnabledUser("weightProposer", "weightProposer@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("weightMaster", "weightMaster@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        createEnabledUser("weightVoter", "weightVoter@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("weightProposer", "SecurePass1!");
        String masterToken = loginAndGetToken("weightMaster", "SecurePass1!");
        String voterToken = loginAndGetToken("weightVoter", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "temaW.pdf", "Tema W", "Desc W");

        Proposal beforeVoting = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNull(beforeVoting.getMasterVoteWeight());
        org.junit.jupiter.api.Assertions.assertNull(beforeVoting.getUserVoteWeight());

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, false)
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk());

        Proposal afterFirstVote = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(2.0, afterFirstVote.getMasterVoteWeight());
        org.junit.jupiter.api.Assertions.assertEquals(1.0, afterFirstVote.getUserVoteWeight());

        voteConfigService.updateConfig(2, 0.5, 3, 0.5, 1.0);

        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/uploadProposal/{id}", proposalId)
                        .header("Authorization", bearer(proposerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void closeExpiredProposalsShouldRecordAuditWithSystemActor() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("auditProposer", "auditProposer@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("auditMaster", "auditMaster@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        String token = loginAndGetTokenQuietly("auditProposer", "SecurePass1!");
        String masterToken = loginAndGetToken("auditMaster", "SecurePass1!");

        long proposalId = uploadProposalAndGetIdQuietly(token, subjectId, "audit.pdf", "Audit", "Desc");

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        proposal.setEndsAt(java.time.Instant.now().minusSeconds(60));
        proposalRepository.save(proposal);

        voteService.closeExpiredProposals();

        MvcResult auditResult = mockMvc.perform(get("/admin/audit")
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode payload = objectMapper.readTree(auditResult.getResponse().getContentAsString());
        boolean foundSystemActor = false;
        for (JsonNode entry : payload.get("content")) {
            if ("[SYSTEM]".equals(entry.get("actor").asText())) {
                foundSystemActor = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundSystemActor,
                "Se espera una auditoría registrada sin actor resuelta como [SYSTEM]");
    }

    @Test
    void demoteShouldCreateExpulsionProposalAndApplyRoleDowngradeWhenApproved() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(2, 0.5, 3);

        User candidate = createEnabledUser("masterCandidate", "masterCandidate@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        createEnabledUser("requester", "requester@rai.usc.es", "SecurePass1!", UserRole.MASTER);
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
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        User candidate = createEnabledUser("plainUser", "plainUser@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("requester2", "requester2@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        String requesterToken = loginAndGetToken("requester2", "SecurePass1!");

        mockMvc.perform(post("/promoteProposal/demote/{id}", candidate.getId())
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ROLE_OPERATION"));
    }

    @Test
    void demoteShouldReturnNotFoundWhenCandidateDoesNotExist() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        createEnabledUser("requester3", "requester3@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        String requesterToken = loginAndGetToken("requester3", "SecurePass1!");

        mockMvc.perform(post("/promoteProposal/demote/{id}", 99999L)
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void closeExpiredProposalsShouldRejectPendingProposalsPastEndsAt() {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("expirer", "expirer@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetTokenQuietly("expirer", "SecurePass1!");

        long proposalId = uploadProposalAndGetIdQuietly(token, subjectId, "tema.pdf", "Tema", "Desc");

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        proposal.setEndsAt(java.time.Instant.now().minusSeconds(60));
        proposalRepository.save(proposal);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(1, closed);
        Proposal refreshed = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.REJECTED, refreshed.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(refreshed.getClosedAt());
    }

    @Test
    void closeExpiredProposalsShouldLeavePendingProposalsNotYetEnded() {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("futurist", "futurist@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetTokenQuietly("futurist", "SecurePass1!");

        long proposalId = uploadProposalAndGetIdQuietly(token, subjectId, "tema.pdf", "Tema", "Desc");

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        proposal.setEndsAt(java.time.Instant.now().plusSeconds(3600));
        proposalRepository.save(proposal);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(0, closed);
        Proposal refreshed = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.PENDING, refreshed.getStatus());
    }

    @Test
    void closeExpiredProposalsShouldOnlyRejectPendingProposals() {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        Long subjectId = createSubject();
        User proposer = createEnabledUser("mixproposer", "mixproposer@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetTokenQuietly("mixproposer", "SecurePass1!");

        long expiredPendingId = uploadProposalAndGetIdQuietly(token, subjectId, "a.pdf", "A", "A");
        long futurePendingId = uploadProposalAndGetIdQuietly(token, subjectId, "b.pdf", "B", "B");

        Proposal expiredPending = proposalRepository.findById(expiredPendingId).orElseThrow();
        expiredPending.setEndsAt(java.time.Instant.now().minusSeconds(60));
        proposalRepository.save(expiredPending);

        Proposal futurePending = proposalRepository.findById(futurePendingId).orElseThrow();
        futurePending.setEndsAt(java.time.Instant.now().plusSeconds(3600));
        proposalRepository.save(futurePending);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(1, closed);
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.REJECTED,
                proposalRepository.findById(expiredPendingId).orElseThrow().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.PENDING,
                proposalRepository.findById(futurePendingId).orElseThrow().getStatus());
    }

    @Test
    void closeExpiredProposalsShouldNotTouchApprovedProposals() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(2, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("approver", "approver@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterX1", "voterX1@rai.usc.es", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterX2", "voterX2@rai.usc.es", "SecurePass1!", UserRole.USER);

        String proposerToken = loginAndGetToken("approver", "SecurePass1!");
        String voter1 = loginAndGetToken("voterX1", "SecurePass1!");
        String voter2 = loginAndGetToken("voterX2", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(proposerToken, subjectId, "aprobada.pdf", "Aprobada", "Desc");
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true).header("Authorization", bearer(voter2)))
                .andExpect(status().isOk());

        Proposal approved = proposalRepository.findById(proposalId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.APPROVED, approved.getStatus());
        approved.setEndsAt(java.time.Instant.now().minusSeconds(60));
        proposalRepository.save(approved);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(0, closed);
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.APPROVED,
                proposalRepository.findById(proposalId).orElseThrow().getStatus());
    }

    @Test
    void closeExpiredProposalsShouldRejectExpiredPromotionProposal() {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        User proposer = createEnabledUser("promproposer", "promproposer@rai.usc.es", "SecurePass1!", UserRole.MASTER);
        User candidate = createEnabledUser("promcandidate", "promcandidate@rai.usc.es", "SecurePass1!", UserRole.USER);

        PromotionProposal promotionProposal = new PromotionProposal();
        promotionProposal.setProposer(proposer);
        promotionProposal.setCandidate(candidate);
        promotionProposal.setActionType(ActionType.PROMOTION);
        promotionProposal.setQuorumRequired(1);
        promotionProposal.setApprovalThreshold(0.5);
        promotionProposal.setEndsAt(java.time.Instant.now().minusSeconds(60));
        promotionProposalRepository.save(promotionProposal);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(1, closed);
        PromotionProposal refreshed = promotionProposalRepository.findById(promotionProposal.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.REJECTED, refreshed.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(refreshed.getClosedAt());
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.USER,
                userRepository.findById(candidate.getId()).orElseThrow().getRole());
    }

    @Test
    void closeExpiredProposalsShouldPenalizeUploaderReputation() {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(1, 0.5, 3);

        Long subjectId = createSubject();
        createEnabledUser("penalized", "penalized@rai.usc.es", "SecurePass1!", UserRole.USER);
        String token = loginAndGetTokenQuietly("penalized", "SecurePass1!");

        long proposalId = uploadProposalAndGetIdQuietly(token, subjectId, "penalizada.pdf", "Penalizada", "Desc");

        User uploader = userRepository.findByUsername("penalized").orElseThrow();
        double reputationBefore = uploader.getReputation();

        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        proposal.setEndsAt(java.time.Instant.now().minusSeconds(60));
        proposalRepository.save(proposal);

        int closed = voteService.closeExpiredProposals();

        org.junit.jupiter.api.Assertions.assertEquals(1, closed);
        User refreshed = userRepository.findById(uploader.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(reputationBefore - 5.0, refreshed.getReputation(), 0.001);
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

    private String loginAndGetTokenQuietly(String username, String password) {
        try {
            return loginAndGetToken(username, password);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo iniciar sesión en el test", e);
        }
    }

    private long uploadProposalAndGetIdQuietly(String token, Long subjectId, String originalFilename, String title, String description) {
        try {
            return uploadProposalAndGetId(token, subjectId, originalFilename, title, description);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo subir la propuesta en el test", e);
        }
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
