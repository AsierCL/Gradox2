package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.thread.CommentResponse;
import com.example.gradox2.presentation.dto.thread.CreateCommentRequest;
import com.example.gradox2.presentation.dto.thread.EditCommentRequest;
import com.example.gradox2.service.interfaces.IForumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("files")
@Validated
@Tag(name = "06. Hilos / Comentarios", description = "Gestión de hilos de discusión por archivo: comentarios, respuestas, referencias a archivos y bloqueo")
public class ForumController {

    private final IForumService forumService;

    public ForumController(IForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Listar comentarios", description = "Lista los comentarios del hilo de un archivo (paginado, visibilidad del archivo aplicada)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devuelta"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado o sin acceso", content = @Content)
    })
    public ResponseEntity<List<CommentResponse>> getThreadComments(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return ResponseEntity.ok(forumService.getThreadComments(id, page, size));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Crear comentario", description = "Publica un comentario en el hilo del archivo (crea el hilo si no existe). Puede responder a otro comentario y/o referenciar un archivo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comentario publicado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, hilo bloqueado o padre no válido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado o sin acceso", content = @Content)
    })
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(forumService.createComment(id, request));
    }

    @PutMapping("/{id}/comments/{commentId}")
    @Operation(summary = "Editar comentario", description = "Edita el contenido de un comentario propio (marca editedAt)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comentario actualizado"),
        @ApiResponse(responseCode = "400", description = "No eres el autor del comentario", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo o comentario no encontrado", content = @Content)
    })
    public ResponseEntity<CommentResponse> editComment(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @Parameter(description = "ID del comentario") @PathVariable @Positive Long commentId,
            @Valid @RequestBody EditCommentRequest request) {
        return ResponseEntity.ok(forumService.editComment(id, commentId, request.getContent()));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @Operation(summary = "Eliminar comentario", description = "Elimina un comentario (autor o MASTER). Si era el último, se elimina el hilo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comentario eliminado"),
        @ApiResponse(responseCode = "400", description = "Sin permiso para eliminar", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo o comentario no encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id,
            @Parameter(description = "ID del comentario") @PathVariable @Positive Long commentId) {
        forumService.deleteComment(id, commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/thread/lock")
    @Operation(summary = "Bloquear hilo", description = "Bloquea el hilo del archivo (uploader o MASTER): no se admiten nuevos comentarios")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hilo bloqueado"),
        @ApiResponse(responseCode = "400", description = "Sin permiso para bloquear", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo o hilo no encontrado", content = @Content)
    })
    public ResponseEntity<Void> lockThread(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        forumService.setThreadLocked(id, true);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/thread/lock")
    @Operation(summary = "Desbloquear hilo", description = "Desbloquea el hilo del archivo (uploader o MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hilo desbloqueado"),
        @ApiResponse(responseCode = "400", description = "Sin permiso para desbloquear", content = @Content),
        @ApiResponse(responseCode = "404", description = "Archivo o hilo no encontrado", content = @Content)
    })
    public ResponseEntity<Void> unlockThread(
            @Parameter(description = "ID del archivo") @PathVariable @Positive Long id) {
        forumService.setThreadLocked(id, false);
        return ResponseEntity.ok().build();
    }
}