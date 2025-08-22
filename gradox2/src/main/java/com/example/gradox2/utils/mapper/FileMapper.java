package com.example.gradox2.utils.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.presentation.dto.files.FileResponse;

@Mapper
public interface FileMapper {
    FileMapper mapper = Mappers.getMapper(FileMapper.class);

FileResponse toFileResponse(File file);
}
