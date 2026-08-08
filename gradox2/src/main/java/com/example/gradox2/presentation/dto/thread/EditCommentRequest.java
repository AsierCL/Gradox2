package com.example.gradox2.presentation.dto.thread;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditCommentRequest {

    @NotBlank(message = "El contenido del comentario es obligatorio")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    private String content;
}