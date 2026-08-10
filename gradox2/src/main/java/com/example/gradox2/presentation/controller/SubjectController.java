package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.subject.SubjectResponse;
import com.example.gradox2.service.interfaces.ISubjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/subjects")
@Tag(name = "Asignaturas", description = "Catálogo de asignaturas y cursos para crear propuestas de archivo")
public class SubjectController {

    private final ISubjectService subjectService;

    public SubjectController(ISubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "Listar asignaturas", description = "Devuelve todas las asignaturas ordenadas por curso y código")
    @ApiResponse(responseCode = "200", description = "Lista de asignaturas devuelta")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver asignatura", description = "Devuelve el detalle de una asignatura con su curso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Asignatura devuelta"),
        @ApiResponse(responseCode = "404", description = "Asignatura no encontrada")
    })
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getById(id));
    }
}