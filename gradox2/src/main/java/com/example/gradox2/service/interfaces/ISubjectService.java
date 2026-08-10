package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.subject.SubjectResponse;

public interface ISubjectService {
    List<SubjectResponse> listAll();

    SubjectResponse getById(Long id);
}