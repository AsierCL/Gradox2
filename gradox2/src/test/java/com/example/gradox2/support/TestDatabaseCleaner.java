package com.example.gradox2.support;

import org.springframework.stereotype.Component;

import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ForumThreadRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
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

/**
 * Vacía la base de datos de los tests respetando las claves foráneas.
 *
 * <p>El perfil {@code test} usa una H2 en memoria compartida por todas las clases
 * que comparten contexto Spring, así que cada clase tiene que limpiar lo que
 * dejaron las anteriores. Borrar solo {@code users} falla contra la FK de
 * {@code file_proposal}/{@code proposals} según el orden en que corra Surefire;
 * el orden de borrado tiene que estar en un único lugar para que no vuelva a
 * quedar incompleto en una clase y completo en otra.
 */
@Component
public class TestDatabaseCleaner {

    private final ThreadCommentRepository threadCommentRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final ScoreRepository scoreRepository;
    private final VoteRepository voteRepository;
    private final PromotionProposalRepository promotionProposalRepository;
    private final FileProposalRepository fileProposalRepository;
    private final ProposalRepository proposalRepository;
    private final FileRepository fileRepository;
    private final TempFileRepository tempFileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VoteConfigRepository voteConfigRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public TestDatabaseCleaner(ThreadCommentRepository threadCommentRepository,
            ForumThreadRepository forumThreadRepository,
            ScoreRepository scoreRepository,
            VoteRepository voteRepository,
            PromotionProposalRepository promotionProposalRepository,
            FileProposalRepository fileProposalRepository,
            ProposalRepository proposalRepository,
            FileRepository fileRepository,
            TempFileRepository tempFileRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            VerificationTokenRepository verificationTokenRepository,
            VoteConfigRepository voteConfigRepository,
            SubjectRepository subjectRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.threadCommentRepository = threadCommentRepository;
        this.forumThreadRepository = forumThreadRepository;
        this.scoreRepository = scoreRepository;
        this.voteRepository = voteRepository;
        this.promotionProposalRepository = promotionProposalRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.proposalRepository = proposalRepository;
        this.fileRepository = fileRepository;
        this.tempFileRepository = tempFileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.voteConfigRepository = voteConfigRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /** Borra de hijos a padres: primero lo que referencia, después lo referenciado. */
    public void clean() {
        threadCommentRepository.deleteAll();
        forumThreadRepository.deleteAll();
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
}
