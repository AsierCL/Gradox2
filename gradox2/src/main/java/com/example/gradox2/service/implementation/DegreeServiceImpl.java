package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.presentation.dto.degree.DegreeResponse;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IDegreeService;

@Service
public class DegreeServiceImpl implements IDegreeService {

    private final DegreeRepository degreeRepository;

    public DegreeServiceImpl(DegreeRepository degreeRepository) {
        this.degreeRepository = degreeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("degrees")
    public List<DegreeResponse> listAll() {
        return degreeRepository.findAllByOrderByCodeAsc().stream()
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

    private DegreeResponse toResponse(Degree degree) {
        return DegreeResponse.builder()
                .id(degree.getId())
                .code(degree.getCode())
                .name(degree.getName())
                .coursesCount(degree.getCourses() != null ? degree.getCourses().size() : 0)
                .build();
    }
}
