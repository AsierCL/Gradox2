package com.example.gradox2.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.gradox2.service.interfaces.IVoteService;

@Component
public class ProposalExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProposalExpirationScheduler.class);

    private final IVoteService voteService;

    public ProposalExpirationScheduler(IVoteService voteService) {
        this.voteService = voteService;
    }

    @Scheduled(fixedDelayString = "${app.proposal-expiration-check-ms:60000}")
    public void closeExpiredProposals() {
        try {
            int closed = voteService.closeExpiredProposals();
            if (closed > 0) {
                logger.info("Cerradas automáticamente {} propuestas vencidas", closed);
            }
        } catch (Exception e) {
            logger.error("Error al cerrar propuestas vencidas", e);
        }
    }
}
