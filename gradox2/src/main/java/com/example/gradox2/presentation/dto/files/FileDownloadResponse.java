package com.example.gradox2.presentation.dto.files;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta con la URL firmada para descargar un archivo directamente desde el bucket")
public class FileDownloadResponse {
    @Schema(description = "URL firmada (PreSigned URL) de descarga directa, válida durante 120 segundos",
            example = "https://bucket.s3.example.com/object?X-Amz-Signature=...")
    private String url;
}