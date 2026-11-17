package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.presentation.dto.degree.DegreeResponse;
import com.example.gradox2.presentation.dto.subject.SubjectResponse;

public interface IDegreeService {

    List<DegreeResponse> listAll();

    DegreeResponse getById(Long id);

    /** Cursos del grado con sus asignaturas; falla con 404 si el grado no existe. */
    List<CourseResponse> getCourses(Long degreeId);

    /** Asignaturas del grado, en plano, con su curso y sus metadatos. */
    List<SubjectResponse> getSubjects(Long degreeId);
}
