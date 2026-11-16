package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.presentation.dto.degree.DegreeResponse;
import com.example.gradox2.service.interfaces.ICourseService;
import com.example.gradox2.service.interfaces.IDegreeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Positive;

/**
 * Catálogo de grados. Lectura pública (D6): es la fuente del selector de grado del registro y de la
 * pantalla por defecto después del login (§9.1, §9.2).
 */
@RestController
@RequestMapping("/degrees")
@Tag(name = "Grados", description = "Catálogo de grados, cursos y asignaturas")
public class DegreeController {

    private final IDegreeService degreeService;
    private final ICourseService courseService;

    public DegreeController(IDegreeService degreeService, ICourseService courseService) {
        this.degreeService = degreeService;
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Listar grados", description = "Devuelve los grados ordenados por código oficial")
    @ApiResponse(responseCode = "200", description = "Lista de grados")
    public ResponseEntity<List<DegreeResponse>> getAllDegrees() {
        return ResponseEntity.ok(degreeService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un grado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Grado encontrado"),
        @ApiResponse(responseCode = "404", description = "Grado no encontrado", content = @Content)
    })
    public ResponseEntity<DegreeResponse> getDegree(
            @Parameter(description = "ID del grado") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(degreeService.getById(id));
    }

    @GetMapping("/{id}/courses")
    @Operation(summary = "Cursos de un grado",
            description = "Cursos del grado con sus asignaturas. Es la pantalla por defecto del grado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cursos del grado"),
        @ApiResponse(responseCode = "404", description = "Grado no encontrado", content = @Content)
    })
    public ResponseEntity<List<CourseResponse>> getDegreeCourses(
            @Parameter(description = "ID del grado") @PathVariable @Positive Long id) {
        // Valida que el grado exista antes de devolver una lista vacía indistinguible de un ID inventado.
        degreeService.getById(id);
        return ResponseEntity.ok(courseService.listByDegree(id));
    }
}
