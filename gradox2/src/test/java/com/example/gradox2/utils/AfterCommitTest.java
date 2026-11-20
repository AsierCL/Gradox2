package com.example.gradox2.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Semántica de {@link AfterCommit} (WAVE1 S8).
 *
 * <p>Spring invoca {@code afterCommit} solo cuando la transacción confirma, y {@code afterCompletion} en
 * ambos casos. La acción tiene que estar colgada del primero: si estuviera en el segundo, un rollback
 * borraría igual el objeto de S3.
 */
class AfterCommitTest {

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void runsImmediatelyWhenThereIsNoTransaction() {
        AtomicBoolean executed = new AtomicBoolean();

        AfterCommit.run(() -> executed.set(true));

        assertThat(executed).isTrue();
    }

    @Test
    void doesNotRunAtRegistrationTime() {
        AtomicBoolean executed = new AtomicBoolean();
        TransactionSynchronizationManager.initSynchronization();

        AfterCommit.run(() -> executed.set(true));

        assertThat(executed).as("registrar no es ejecutar").isFalse();
    }

    @Test
    void runsWhenTheTransactionCommits() {
        AtomicBoolean executed = new AtomicBoolean();
        TransactionSynchronizationManager.initSynchronization();

        AfterCommit.run(() -> executed.set(true));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        assertThat(executed).isTrue();
    }

    @Test
    void staysSkippedWhenTheTransactionRollsBack() {
        AtomicBoolean executed = new AtomicBoolean();
        TransactionSynchronizationManager.initSynchronization();

        AfterCommit.run(() -> executed.set(true));
        // Un rollback solo dispara afterCompletion: la acción no puede correr.
        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(executed).isFalse();
    }
}
