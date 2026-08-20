package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.course.CourseResponse;

public interface ICourseService {
    List<CourseResponse> listAll();

    CourseResponse getById(Long id);
}