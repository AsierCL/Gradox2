package com.example.gradox2.utils;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Acciones que solo pueden tener efecto si la transacción confirma (WAVE1 S8).
 *
 * <p>Un objeto S3 no participa del rollback de la base: borrarlo dentro de la transacción significa que un
 * error posterior deja la fila viva apuntando a un archivo que ya no existe. Es pérdida de datos
 * irreversible, así que el borrado se registra para después del commit.
 *
 * <p>Fuera de una transacción la acción corre de inmediato: no hay nada que revertir.
 */
public final class AfterCommit {

    private AfterCommit() {
    }

    public static void run(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
