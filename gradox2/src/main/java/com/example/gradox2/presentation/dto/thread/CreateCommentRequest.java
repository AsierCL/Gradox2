package com.example.gradox2.presentation.dto.thread;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequest {

    @NotBlank(message = "El contenido del comentario es obligatorio")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    private String content;

    @Positive(message = "El archivo referenciado debe ser un id válido")
    private Long referencedFileId;

    @Positive(message = "El comentario padre debe ser un id válido")
    private Long parentCommentId;
}