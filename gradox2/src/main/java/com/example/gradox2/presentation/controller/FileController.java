package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.service.interfaces.IFileService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;



@RestController
@RequestMapping("/files")
@Validated
@Tag(name = "05. Archivos", description = "Gestión de archivos publicados: consulta, descarga, votación, visibilidad y propuestas de borrado")
public class FileController {
    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/all")
    @Operation(summary = "Listar archivos", description = "Lista todos los archivos publicados (con visibilidad aplicada)")
    @ApiResponse(responseCode = "200", description = "Lista devuelta")
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de archivo", description = "Obtiene los metadatos de un archivo publicados (visibilidad aplicada sobre el uploader)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos devueltos"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    public ResponseEntity<FileResponse> getFile(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileService.getFile(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar archivo", description = "Descarga el contenido del archivo publicado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo descargado"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    public ResponseEntity<ByteArrayResource> downloadFile(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        return fileService.downloadFile(id);
    }

    @PostMapping("/upload")
    @Operation(summary = "Subir archivo (legacy)", description = "Crea directamente una propuesta de subida. Preferir /uploadProposal/upload que devuelve respuesta estructurada.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo enviado para revisión"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<String> uploadFile(@Valid @ModelAttribute UploadFileProposalRequest uploadFileRequest){
        return fileService.uploadFile(uploadFileRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Proponer borrado", description = "Crea una propuesta de eliminación para un archivo publicado (requiere votación)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta de borrado creada"),
        @ApiResponse(responseCode = "400", description = "Ya existe una propuesta de borrado activa", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    public ResponseEntity<FileProposalResponse> requestFileDeletion(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileService.requestFileDeletion(id));
    }

    @PostMapping("/{id}/vote/{upvote}")
    @Operation(summary = "Votar archivo", description = "Vota positivo (true) o negativo (false) para puntuar un archivo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voto registrado/cambiado"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    public ResponseEntity<VoteResponse> voteFile(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @Parameter(description = "true = positivo, false = negativo") @PathVariable boolean upvote) {
        return ResponseEntity.ok(fileService.voteFile(id, upvote));
    }

    @DeleteMapping("/{id}/vote")
    @Operation(summary = "Retirar voto", description = "Elimina el voto del usuario autenticado en un archivo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voto retirado"),
        @ApiResponse(responseCode = "404", description = "Archivo o voto no encontrado", content = @Content)
    })
    public ResponseEntity<VoteResponse> retractVote(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileService.retractVote(id));
    }

    @PutMapping("/{id}/visibility")
    @Operation(summary = "Cambiar visibilidad", description = "Cambia el nivel de visibilidad del uploader del archivo (solo el propietario o MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visibilidad actualizada"),
        @ApiResponse(responseCode = "400", description = "No tienes permiso para cambiar la visibilidad", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    public ResponseEntity<FileResponse> updateFileVisibility(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @Parameter(description = "Nuevo nivel de visibilidad (PUBLIC, RESTRICTED, PRIVATE)") @RequestBody @NotNull FileVisibility visibilityLevel) {
        return ResponseEntity.ok(fileService.updateFileVisibility(id, visibilityLevel));
    }

}
