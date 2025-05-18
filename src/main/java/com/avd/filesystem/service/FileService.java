package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    FileDto uploadFile(MultipartFile file, FileDto fileDto, Long groupId);
    List<FileDto> uploadBundle(List<MultipartFile> files, FileDto fileDto, Long groupId, String bundleName);
    FileDto getFile(Long id);
    List<FileDto> getFiles(Long uploadedBy, Long groupId);
    void deleteFile(Long id);
    boolean userHasAccessToFile(String username, Long fileId);
    java.io.File getPhysicalFile(Long fileId);
}
