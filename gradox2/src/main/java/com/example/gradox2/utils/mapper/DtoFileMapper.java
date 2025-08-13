package com.example.gradox2.utils.mapper;

public class DtoFileMapper {
    public static FileResponse toFileResponse(File fileEntity) {
        FileResponse response = new FileResponse();
        response.setFileName(fileEntity.getFileName());
        response.setFileType(fileEntity.getFileType());
        response.setSubject(fileEntity.getSubject());
        response.setFile(fileEntity.getFileData());
        return response;
    }

    public static File toFileEntity(FileResponse fileResponse) {
        File fileEntity = new File();
        fileEntity.setFileName(fileResponse.getFileName());
        fileEntity.setFileType(fileResponse.getFileType());
        fileEntity.setSubject(fileResponse.getSubject());
        fileEntity.setFileData(fileResponse.getFile());
        return fileEntity;
    }
}