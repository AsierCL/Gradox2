package com.example.gradox2.utils.mapper;

import java.util.Comparator;
import java.util.List;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.presentation.dto.course.CourseResponse;
import com.example.gradox2.presentation.dto.course.CourseSubjectResponse;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static final CourseResponse toCourseResponse(Course course) {
        if (course == null) {
            return null;
        }

        List<CourseSubjectResponse> subjects = course.getSubjects() == null
                ? List.of()
                : course.getSubjects().stream()
                        .sorted(Comparator.comparing(Subject::getCode))
                        .map(CourseMapper::toCourseSubjectResponse)
                        .toList();

        return CourseResponse.builder()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .degreeId(course.getDegree() != null ? course.getDegree().getId() : null)
            .degreeCode(course.getDegree() != null ? course.getDegree().getCode() : null)
            .degreeName(course.getDegree() != null ? course.getDegree().getName() : null)
            .subjects(subjects)
            .build();
    }

    private static CourseSubjectResponse toCourseSubjectResponse(Subject subject) {
        return CourseSubjectResponse.builder()
            .id(subject.getId())
            .code(subject.getCode())
            .name(subject.getName())
            .credits(subject.getCredits())
            .semester(subject.getSemester())
            .subjectType(subject.getSubjectType())
            .build();
    }
}