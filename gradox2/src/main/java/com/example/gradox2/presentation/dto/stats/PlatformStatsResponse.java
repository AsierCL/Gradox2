package com.example.gradox2.presentation.dto.stats;

import java.util.List;

import com.example.gradox2.persistence.entities.enums.FileType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Métricas colectivas de la plataforma para la comunidad")
public class PlatformStatsResponse {

    @Schema(description = "Número total de archivos publicados", example = "124")
    private long totalFiles;

    @Schema(description = "Número total de usuarios registrados", example = "532")
    private long totalUsers;

    @Schema(description = "Tamaño total almacenado en bytes", example = "1536000000")
    private long totalStorageBytes;

    @Schema(description = "Número total de descargas realizadas", example = "8421")
    private long totalDownloads;

    @Schema(description = "Desglose de archivos por tipo")
    private List<TypeStat> byType;

    @Schema(description = "Desglose de archivos por asignatura")
    private List<SubjectStat> bySubject;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Archivos por tipo")
    public static class TypeStat {
        @Schema(description = "Tipo de archivo", example = "APUNTES")
        private FileType type;

        @Schema(description = "Número de archivos de este tipo", example = "80")
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Archivos por asignatura")
    public static class SubjectStat {
        @Schema(description = "Código de la asignatura", example = "MAT1")
        private String code;

        @Schema(description = "Nombre de la asignatura", example = "Matemáticas I")
        private String name;

        @Schema(description = "Nombre del curso al que pertenece", example = "Primer curso")
        private String courseName;

        @Schema(description = "Número de archivos de esta asignatura", example = "12")
        private long count;
    }
}