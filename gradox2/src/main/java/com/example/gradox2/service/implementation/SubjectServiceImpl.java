package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.presentation.dto.subject.SubjectResponse;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.ISubjectService;
import com.example.gradox2.utils.mapper.SubjectMapper;

@Service
public class SubjectServiceImpl implements ISubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("subjects")
    public List<SubjectResponse> listAll() {
        return subjectRepository.findAllByOrderByCourseIdAscCodeAsc()
                .stream()
                .map(SubjectMapper::toSubjectResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "subjects", key = "#id")
    public SubjectResponse getById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject not found"));
        return SubjectMapper.toSubjectResponse(subject);
    }
}