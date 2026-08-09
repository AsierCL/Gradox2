package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.ForumThread;import com.example.gradox2.persistence.entities.ThreadComment;
import com.example.gradox2.persistence.entities.User;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForumThreadIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(800);

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

    @BeforeEach
    void setUp() {
        // La BD H2 se comparte entre clases de test; borrar en orden FK-safe
        // (hijos antes que padres).
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
    }

    @Test
    void createCommentShouldCreateThreadImplicitlyAndReturnComment() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.usc.es", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String token = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Primer comentario"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.content").value("Primer comentario"))
                .andExpect(jsonPath("$.authorUsername").value("alice"))
                .andExpect(jsonPath("$.parentCommentId").doesNotExist())
                .andExpect(jsonPath("$.referencedFile").doesNotExist())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.editedAt").doesNotExist());

        org.junit.jupiter.api.Assertions.assertEquals(1, forumThreadRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, threadCommentRepository.count());
    }

    @Test
    void createCommentWithoutAuthenticationShouldBeRejected() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Sin sesion"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCommentWithBlankContentShouldBeRejected() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String token = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void replyToCommentOfSameThreadShouldBeAllowed() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);

        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Pregunta"))))
                .andExpect(status().isOk())
                .andReturn();
        long parentId = readResponse(first);

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Respuesta", "parentCommentId", parentId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCommentId").value(parentId));

        mockMvc.perform(get("/files/{id}/comments", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void replyToCommentOfOtherThreadShouldBeRejected() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File fileA = createPublishedFile(uploader, FileVisibility.PUBLIC);
        File fileB = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String token = loginAndGetToken("alice", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", fileA.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "En el archivo A"))))
                .andExpect(status().isOk())
                .andReturn();
        long parentId = readResponse(first);

        mockMvc.perform(post("/files/{id}/comments", fileB.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Respondo desde B", "parentCommentId", parentId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));
    }

    @Test
    void createCommentReferencingVisibleFileShouldReturnReferenceCard() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File source = createPublishedFile(uploader, FileVisibility.PUBLIC);
        File target = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", source.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Mira este archivo", "referencedFileId", target.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencedFile.id").value(target.getId()))
                .andExpect(jsonPath("$.referencedFile.title").value(target.getTitle()))
                .andExpect(jsonPath("$.referencedFile.fileType").value(FileType.APUNTES.name()))
                .andExpect(jsonPath("$.referencedFile.available").value(true));
    }

    @Test
    void createCommentReferencingInaccessibleOrUnknownFileShouldBeRejected() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        User other = createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File privateFile = createPublishedFile(other, FileVisibility.PRIVATE);
        File fileA = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", fileA.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Referencia oculta", "referencedFileId", privateFile.getId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));

        mockMvc.perform(post("/files/{id}/comments", fileA.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Referencia inexistente", "referencedFileId", 999999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void editCommentShouldOnlyAllowTheAuthor() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Original"))))
                .andExpect(status().isOk())
                .andReturn();
        long commentId = readResponse(first);

        mockMvc.perform(put("/files/{id}/comments/{commentId}", file.getId(), commentId)
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Invasión"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        mockMvc.perform(put("/files/{id}/comments/{commentId}", file.getId(), commentId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Edición propia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Edición propia"))
                .andExpect(jsonPath("$.editedAt").isNotEmpty());
    }

    @Test
    void deleteCommentShouldOnlyAllowAuthorOrMaster() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("master", "master@rai.test", "SecurePass1!", UserRole.MASTER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");
        String masterToken = loginAndGetToken("master", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isOk())
                .andReturn();
        long commentId = readResponse(first);

        mockMvc.perform(delete("/files/{id}/comments/{commentId}", file.getId(), commentId)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        mockMvc.perform(delete("/files/{id}/comments/{commentId}", file.getId(), commentId)
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(0, threadCommentRepository.count());
    }

    @Test
    void deletingLastCommentShouldRemoveTheThread() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Único comentario"))))
                .andExpect(status().isOk())
                .andReturn();
        long commentId = readResponse(first);

        mockMvc.perform(delete("/files/{id}/comments/{commentId}", file.getId(), commentId)
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(0, forumThreadRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, threadCommentRepository.count());
    }

    @Test
    void nestedRepliesShouldBeAllowedAtAnyDepth() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        long rootId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Raiz"))))
                .andExpect(status().isOk())
                .andReturn());

        long childId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Hijo", "parentCommentId", rootId))))
                .andExpect(status().isOk())
                .andReturn());

        long grandchildId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Nieto", "parentCommentId", childId))))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(get("/files/{id}/comments", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(rootId))
                .andExpect(jsonPath("$[0].parentCommentId").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(childId))
                .andExpect(jsonPath("$[1].parentCommentId").value(rootId))
                .andExpect(jsonPath("$[2].id").value(grandchildId))
                .andExpect(jsonPath("$[2].parentCommentId").value(childId));
    }

    @Test
    void deletingParentCommentShouldRemoveWholeSubtreeWithIt() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        User bob = createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        MvcResult first = mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Padre"))))
                .andExpect(status().isOk())
                .andReturn();
        long parentId = readResponse(first);

        long childId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Hijo", "parentCommentId", parentId))))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Nieto", "parentCommentId", childId))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/files/{id}/comments/{commentId}", file.getId(), parentId)
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(0, threadCommentRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, forumThreadRepository.count());

        mockMvc.perform(get("/files/{id}/comments", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deletingMiddleCommentShouldKeepAncestorsAndSiblings() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        long rootId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Raiz"))))
                .andExpect(status().isOk())
                .andReturn());

        long childId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Hijo", "parentCommentId", rootId))))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Nieto", "parentCommentId", childId))))
                .andExpect(status().isOk());

        long siblingId = readResponse(mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Hermano de la raiz"))))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(delete("/files/{id}/comments/{commentId}", file.getId(), childId)
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(2, threadCommentRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, forumThreadRepository.count());

        mockMvc.perform(get("/files/{id}/comments", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(rootId))
                .andExpect(jsonPath("$[1].id").value(siblingId));
    }

    @Test
    void lockThreadShouldBlockNewCommentsAndUnlockShouldRestoreThem() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Primero"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/files/{id}/thread/lock", file.getId())
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Bloqueado"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        mockMvc.perform(delete("/files/{id}/thread/lock", file.getId())
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Desbloqueado"))))
                .andExpect(status().isOk());
    }

    @Test
    void lockThreadShouldOnlyBeAllowedForUploaderOrMaster() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        User master = createEnabledUser("master", "master@rai.test", "SecurePass1!", UserRole.MASTER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String bobToken = loginAndGetToken("bob", "SecurePass1!");
        String masterToken = loginAndGetToken("master", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Primero"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/files/{id}/thread/lock", file.getId())
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));

        mockMvc.perform(post("/files/{id}/thread/lock", file.getId())
                        .header("Authorization", bearer(masterToken)))
                .andExpect(status().isOk());
    }

    @Test
    void replyToUnknownCommentShouldBeRejected() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario", "parentCommentId", 999999L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_OPERATION"));
    }

    @Test
    void privateInterpolationShouldBeHidden() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File privateFile = createPublishedFile(uploader, FileVisibility.PRIVATE);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", privateFile.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario privado"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/files/{id}/comments", privateFile.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/files/{id}/comments", privateFile.getId())
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/files/{id}/comments", privateFile.getId())
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void restrictedThreadShouldBeVisibleOnlyToAuthenticatedUsers() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        User bob = createEnabledUser("bob", "bob@rai.test", "SecurePass1!", UserRole.USER);
        File restrictedFile = createPublishedFile(uploader, FileVisibility.RESTRICTED);
        String aliceToken = loginAndGetToken("alice", "SecurePass1!");
        String bobToken = loginAndGetToken("bob", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", restrictedFile.getId())
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario restringido"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/files/{id}/comments", restrictedFile.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/files/{id}/comments", restrictedFile.getId())
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorUsername").value("alice"));
    }

    @Test
    void paginationShouldCapSizeAndStepByPage() throws Exception {
        User uploader = createEnabledUser("alice", "alice@rai.test", "SecurePass1!", UserRole.USER);
        File file = createPublishedFile(uploader, FileVisibility.PUBLIC);
        String token = loginAndGetToken("alice", "SecurePass1!");

        mockMvc.perform(post("/files/{id}/comments", file.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario 0"))))
                .andExpect(status().isOk());

        ForumThread thread = forumThreadRepository.findAll().get(0);
        List<ThreadComment> bulk = IntStream.rangeClosed(1, 120)
                .mapToObj(i -> ThreadComment.builder()
                        .thread(thread)
                        .author(uploader)
                        .content("Bulk " + i)
                        .build())
                .toList();
        threadCommentRepository.saveAll(bulk);

        mockMvc.perform(get("/files/{id}/comments", file.getId()).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Comentario 0"))
                .andExpect(jsonPath("$[1].content").value("Bulk 1"));

        mockMvc.perform(get("/files/{id}/comments", file.getId()).param("page", "60").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Bulk 120"));

        mockMvc.perform(get("/files/{id}/comments", file.getId()).param("page", "0").param("size", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100))
                .andExpect(jsonPath("$[99].content").value("Bulk 99"));

        mockMvc.perform(get("/files/{id}/comments", file.getId()).param("page", "1").param("size", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(21));
    }

    @Test
    void fileDeletionApprovedByGovernanceShouldRemoveThreadAndComments() throws Exception {
        voteConfigService.reloadConfig();
        voteConfigService.updateConfig(2, 0.5, 3);
        Long subjectId = createSubject();

        createEnabledUser("uploadGov", "uploadGov@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("voterGov", "voterGov@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("commenterGov", "commenterGov@rai.test", "SecurePass1!", UserRole.USER);
        createEnabledUser("deleterGov", "deleterGov@rai.test", "SecurePass1!", UserRole.USER);

        String uploadToken = loginAndGetToken("uploadGov", "SecurePass1!");
        String voterToken = loginAndGetToken("voterGov", "SecurePass1!");
        String commenterToken = loginAndGetToken("commenterGov", "SecurePass1!");
        String deleterToken = loginAndGetToken("deleterGov", "SecurePass1!");

        long proposalId = uploadProposalAndGetId(uploadToken, subjectId, "governed.pdf", "Tema gobernado", "Desc");
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(uploadToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", proposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());

        File published = fileRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(post("/files/{id}/comments", published.getId())
                        .header("Authorization", bearer(commenterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario sobre archivo gobernado"))))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(1, forumThreadRepository.count());

        MvcResult result = mockMvc.perform(delete("/files/{id}", published.getId())
                        .header("Authorization", bearer(deleterToken)))
                .andExpect(status().isOk())
                .andReturn();
        long deleteProposalId = readProposalId(result);

        mockMvc.perform(post("/vote/{id}/{upvote}", deleteProposalId, true)
                        .header("Authorization", bearer(voterToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vote/{id}/{upvote}", deleteProposalId, true)
                        .header("Authorization", bearer(deleterToken)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertTrue(fileRepository.findById(published.getId()).isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(0, forumThreadRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, threadCommentRepository.count());
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

    private File createPublishedFile(User uploader, FileVisibility visibility) {
        Course course = courseRepository.save(Course.builder()
                .code("TC" + System.nanoTime())
                .name("Curso Thread")
                .build());
        Subject subject = subjectRepository.save(Subject.builder()
                .code("TS" + System.nanoTime())
                .name("Asignatura Thread")
                .course(course)
                .build());
        return fileRepository.save(File.builder()
                .title("apuntes-" + System.nanoTime() + ".pdf")
                .description("Descripcion")
                .type(FileType.APUNTES)
                .objectKey("key-" + System.nanoTime())
                .uploader(uploader)
                .subject(subject)
                .visibilityLevel(visibility)
                .build());
    }

    private Long createSubject() {
        Course course = courseRepository.save(Course.builder()
                .code("TC" + System.nanoTime())
                .name("Curso Test")
                .build());

        Subject subject = subjectRepository.save(Subject.builder()
                .code("TS" + System.nanoTime())
                .name("Asignatura Test")
                .course(course)
                .build());

        return subject.getId();
    }

    private long uploadProposalAndGetId(String token, Long subjectId, String originalFilename, String title,
            String description) throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                MediaType.APPLICATION_PDF_VALUE,
                "contenido-de-prueba".getBytes());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/uploadProposal/upload")
                .file(multipartFile)
                .param("title", title)
                .param("description", description)
                .param("type", FileType.APUNTES.name())
                .param("subjectId", String.valueOf(subjectId))
                .header("Authorization", bearer(token));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        return Long.parseLong(result.getResponse().getContentAsString().replaceAll("[^0-9]", ""));
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

    private long readResponse(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private long readProposalId(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
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