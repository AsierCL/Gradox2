package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.degree.DegreeResponse;

public interface IDegreeService {

    List<DegreeResponse> listAll();

    DegreeResponse getById(Long id);
}
