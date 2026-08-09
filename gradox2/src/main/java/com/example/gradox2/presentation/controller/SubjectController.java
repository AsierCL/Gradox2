package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.presentation.dto.subject.SubjectResponse;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.utils.mapper.SubjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/subjects")
@Tag(name = "08. Asignaturas", description = "Catálogo de asignaturas y cursos para crear propuestas de archivo")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    @Operation(summary = "Listar asignaturas", description = "Devuelve todas las asignaturas ordenadas por curso y código")
    @ApiResponse(responseCode = "200", description = "Lista de asignaturas devuelta")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        List<SubjectResponse> subjects = subjectRepository.findAllByOrderByCourseIdAscCodeAsc()
                .stream()
                .map(SubjectMapper::toSubjectResponse)
                .toList();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver asignatura", description = "Devuelve el detalle de una asignatura con su curso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Asignatura devuelta"),
        @ApiResponse(responseCode = "404", description = "Asignatura no encontrada")
    })
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));
        return ResponseEntity.ok(SubjectMapper.toSubjectResponse(subject));
    }
}