package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.repository.ResourceRepository;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.service.interfaces.IFileService;

@Service
public class FileServiceImpl implements IFileService {
    private final ResourceRepository resourceRepository;

    public FileServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public List<FileResponse> getAllFiles() {
    List<File> files = resourceRepository.findAll();
//        return files.stream()
//                .map(file -> new FileResponse(file.getTitle(), file.getType(), file.getSubject(), file.getFileData()))
//                .toList();

        /* return files.stream()
                .map(this::toFileResponse)
                .toList(); */
                return null;
    }


}
