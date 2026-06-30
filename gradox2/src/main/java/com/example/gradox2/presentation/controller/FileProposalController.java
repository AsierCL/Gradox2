package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.service.interfaces.IFileProposalService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


@RestController
@RequestMapping("/uploadProposal")
@Validated
@Tag(name = "04. Propuestas de Archivos", description = "Creación, consulta y borrado de propuestas de subida de archivos")
public class FileProposalController {

    private final IFileProposalService fileProposalService;

    public FileProposalController(IFileProposalService fileProposalService) {
        this.fileProposalService = fileProposalService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Proponer archivo", description = "Sube un archivo para su revisión mediante votación. Se especifica el nivel de visibilidad (PUBLIC, RESTRICTED, PRIVATE)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta creada, pendiente de votación"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o límite de subidas pendientes alcanzado", content = @Content)
    })
    public ResponseEntity<FileProposalResponse> uploadProposal(@Valid @ModelAttribute UploadFileProposalRequest fileProposalRequest) {
        return ResponseEntity.ok(fileProposalService.uploadFileProposal(fileProposalRequest));
    }

    @GetMapping
    @Operation(summary = "Listar propuestas", description = "Lista todas las propuestas de archivos, con soporte de paginación y ordenación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devuelta"),
        @ApiResponse(responseCode = "403", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<?> getAllUploadProposals(
            @Parameter(description = "Activar paginación") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (máx 100)") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo de ordenación (id, quorumRequired, approvalThreshold, status, actionType, createdAt, closedAt)") @RequestParam(defaultValue = "id") String sortBy) {

        if (paged) {
            Page<FileProposalResponse> proposals = fileProposalService.getFileProposalsPaged(page, size, sortBy);
            return ResponseEntity.ok(proposals);
        } else {
            List<FileProposalResponse> proposals = fileProposalService.getAllFileProposals();
            return ResponseEntity.ok(proposals);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de propuesta", description = "Obtiene los datos de una propuesta de archivo por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos devueltos"),
        @ApiResponse(responseCode = "404", description = "Propuesta no encontrada", content = @Content)
    })
    public ResponseEntity<FileProposalResponse> getUploadProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileProposalService.getFileProposalById(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar archivo propuesto", description = "Descarga el archivo temporal asociado a una propuesta pendiente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo descargado"),
        @ApiResponse(responseCode = "404", description = "Propuesta o archivo no encontrado", content = @Content)
    })
    public ResponseEntity<ByteArrayResource> downloadProposalFile(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        TempFile file = fileProposalService.downloadFileFromProposal(id);
        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getTitle())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileData().length)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrar propuesta", description = "Elimina una propuesta pendiente (solo el proponente puede borrarla)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta eliminada"),
        @ApiResponse(responseCode = "400", description = "La propuesta no está pendiente o no eres el proponente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Propuesta no encontrada", content = @Content)
    })
    public ResponseEntity<String> deleteUploadProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileProposalService.deleteFileProposal(id));
    }
}
