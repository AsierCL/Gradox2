package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.service.interfaces.ICourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/courses")
@Tag(name = "Asignaturas", description = "Catálogo de asignaturas y cursos para crear propuestas de archivo")
public class CourseController {

    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Listar cursos", description = "Devuelve todos los cursos ordenados por código con sus asignaturas")
    @ApiResponse(responseCode = "200", description = "Lista de cursos devuelta")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver curso", description = "Devuelve el detalle de un curso con sus asignaturas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso devuelto"),
        @ApiResponse(responseCode = "404", description = "Curso no encontrado")
    })
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getById(id));
    }
}