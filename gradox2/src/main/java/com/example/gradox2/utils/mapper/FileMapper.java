package com.example.gradox2.utils.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.presentation.dto.files.FileResponse;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileMapper INSTANCE = Mappers.getMapper(FileMapper.class);

    @Mapping(target = "uploaderUsername", source = "uploader.username")
    FileResponse toFileResponse(File file);

    @Mapping(target = ".", source = "subject")
    default String subjectToString(com.example.gradox2.persistence.entities.Subject subject) {
        return subject != null ? subject.getName() : null;
    }
}
