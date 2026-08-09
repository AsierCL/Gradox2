package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.presentation.dto.subject.SubjectResponse;

public final class SubjectMapper {

    private SubjectMapper() {
    }

    public static final SubjectResponse toSubjectResponse(Subject subject) {
        if (subject == null) {
            return null;
        }

        return SubjectResponse.builder()
            .id(subject.getId())
            .code(subject.getCode())
            .name(subject.getName())
            .courseId(subject.getCourse() != null ? subject.getCourse().getId() : null)
            .courseCode(subject.getCourse() != null ? subject.getCourse().getCode() : null)
            .courseName(subject.getCourse() != null ? subject.getCourse().getName() : null)
            .build();
    }
}