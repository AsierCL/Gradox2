package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Degree;

import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.presentation.dto.degree.DegreeResponse;
import com.example.gradox2.presentation.dto.subject.SubjectResponse;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.ICourseService;
import com.example.gradox2.service.interfaces.IDegreeService;
import com.example.gradox2.service.interfaces.ISubjectService;

@Service
public class DegreeServiceImpl implements IDegreeService {

    private final DegreeRepository degreeRepository;
    private final ICourseService courseService;
    private final ISubjectService subjectService;

    public DegreeServiceImpl(DegreeRepository degreeRepository, ICourseService courseService,
            ISubjectService subjectService) {
        this.degreeRepository = degreeRepository;
        this.courseService = courseService;
        this.subjectService = subjectService;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("degrees")
    public List<DegreeResponse> listAll() {
        // Trae los cursos en la misma consulta: `coursesCount` no debe disparar una consulta por grado.
        return degreeRepository.findAllWithCoursesOrderedByCode().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "degrees", key = "#id")
    public DegreeResponse getById(Long id) {
        return toResponse(degreeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Degree not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(Long degreeId) {
        requireDegree(degreeId);
        return courseService.listByDegree(degreeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjects(Long degreeId) {
        requireDegree(degreeId);
        return subjectService.listByDegree(degreeId);
    }

    /**
     * Un grado inexistente y un grado sin cursos no deben ser indistinguibles para el cliente:
     * el primero es un 404, el segundo una lista vacía.
     */
    private void requireDegree(Long degreeId) {
        if (!degreeRepository.existsById(degreeId)) {
            throw new NotFoundException("Degree not found");
        }
    }

    private DegreeResponse toResponse(Degree degree) {
        return DegreeResponse.builder()
                .id(degree.getId())
                .code(degree.getCode())
                .name(degree.getName())
                .coursesCount(degree.getCourses() != null ? degree.getCourses().size() : 0)
                .build();
    }
}
