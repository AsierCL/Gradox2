package com.example.gradox2.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.service.implementation.S3StorageService;

import lombok.RequiredArgsConstructor;

/**
 * One-off: rellena el tamaño (sizeBytes) de archivos publicados antes de que
 * la columna existiera, consultando S3 (headObject). Es idempotente: solo
 * procesa registros con tamaño nulo y tolera objetos inaccesibles.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class BackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BackfillRunner.class);

    private final FileRepository fileRepository;
    private final S3StorageService s3StorageService;

    @Override
    @Transactional
    public void run(String... args) {
        var files = fileRepository.findBySizeBytesIsNull();
        if (files.isEmpty()) {
            return;
        }

        int updated = 0;
        for (File file : files) {
            Long size = s3StorageService.sizeOf(file.getObjectKey());
            if (size != null) {
                file.setSizeBytes(size);
                updated++;
            }
        }
        fileRepository.saveAll(files);
        log.info("Backfill de tamaños de archivos completado: {} actualizados de {} pendientes.", updated, files.size());
    }
}