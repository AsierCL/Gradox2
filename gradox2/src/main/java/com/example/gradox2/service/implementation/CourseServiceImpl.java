package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.ICourseService;
import com.example.gradox2.utils.mapper.CourseMapper;

@Service
public class CourseServiceImpl implements ICourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("courses")
    public List<CourseResponse> listAll() {
        return courseRepository.findAllWithSubjectsByOrderByCodeAsc()
                .stream()
                .map(CourseMapper::toCourseResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "#id")
    public CourseResponse getById(Long id) {
        Course course = courseRepository.findByIdWithSubjects(id)
                .orElseThrow(() -> new NotFoundException("Course not found"));
        return CourseMapper.toCourseResponse(course);
    }
}