package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.FileDto;
import com.avd.filesystem.model.entity.*;
import com.avd.filesystem.repository.*;
import com.avd.filesystem.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BundleRepository bundleRepository;
    private final GrantedAccessRepository grantedAccessRepository;
    private final String fileBasePath = "uploads"; // Should be configurable

    @Override
    public FileDto uploadFile(MultipartFile file, FileDto fileDto, Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User uploader = userRepository.findById(fileDto.getUploadedBy()).orElseThrow();
        String filePath = storeFile(file);
        File entity = File.builder()
                .filename(file.getOriginalFilename())
                .filePath(filePath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .description(fileDto.getDescription())
                .clickLocation(fileDto.getClickLocation())
                .clickTime(fileDto.getClickTime())
                .occasion(fileDto.getOccasion())
                .uploadedBy(uploader)
                .group(group)
                .build();
        entity = fileRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public List<FileDto> uploadBundle(List<MultipartFile> files, FileDto fileDto, Long groupId, String bundleName) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User uploader = userRepository.findById(fileDto.getUploadedBy()).orElseThrow();
        Bundle bundle = Bundle.builder()
                .name(bundleName)
                .uploadedBy(uploader)
                .group(group)
                .build();
        final Bundle savedBundle = bundleRepository.save(bundle);
        List<File> fileEntities = files.stream().map(f -> {
            String filePath = storeFile(f);
            return File.builder()
                    .filename(f.getOriginalFilename())
                    .filePath(filePath)
                    .fileType(f.getContentType())
                    .fileSize(f.getSize())
                    .description(fileDto.getDescription())
                    .clickLocation(fileDto.getClickLocation())
                    .clickTime(fileDto.getClickTime())
                    .occasion(fileDto.getOccasion())
                    .uploadedBy(uploader)
                    .group(group)
                    .bundle(savedBundle)
                    .build();
        }).collect(Collectors.toList());
        fileRepository.saveAll(fileEntities);
        return fileEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public FileDto getFile(Long id) {
        return fileRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public List<FileDto> getFiles(Long uploadedBy, Long groupId) {
        return fileRepository.findAll().stream()
                .filter(f -> (uploadedBy == null || f.getUploadedBy().getId().equals(uploadedBy)) &&
                             (groupId == null || f.getGroup().getId().equals(groupId)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(Long id) {
        fileRepository.deleteById(id);
    }

    @Override
    public boolean userHasAccessToFile(String username, Long fileId) {
        // 1. Get user
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        // 2. Superadmin always has access
        if (user.getRole() == User.Role.SUPERADMIN) return true;
        // 3. Get file
        File file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return false;
        // 4. Uploader always has access
        if (file.getUploadedBy().getId().equals(user.getId())) return true;
        // 5. Admin of the group always has access
        boolean isAdmin = user.getUserGroupRoles().stream()
                .anyMatch(r -> r.getGroup().getId().equals(file.getGroup().getId()) && r.getRole() == UserGroupRole.Role.ADMIN);
        if (isAdmin) return true;
        // 6. Check granted access (file or bundle)
        // (a) Direct file access
        boolean hasFileAccess = grantedAccessRepository.findAll().stream()
                .anyMatch(a -> a.getUser().getId().equals(user.getId()) && a.getFile() != null && a.getFile().getId().equals(fileId));
        if (hasFileAccess) return true;
        // (b) Bundle access
        if (file.getBundle() != null) {
            boolean hasBundleAccess = grantedAccessRepository.findAll().stream()
                    .anyMatch(a -> a.getUser().getId().equals(user.getId()) && a.getBundle() != null && a.getBundle().getId().equals(file.getBundle().getId()));
            if (hasBundleAccess) return true;
        }
        return false;
    }

    @Override
    public java.io.File getPhysicalFile(Long fileId) {
        File file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return null;
        return new java.io.File(file.getFilePath());
    }

    private String storeFile(MultipartFile file) {
        try {
            Path dir = Paths.get(fileBasePath);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path filePath = dir.resolve(file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(file.getBytes());
            }
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("File storage failed", e);
        }
    }

    private FileDto toDto(File file) {
        return FileDto.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .description(file.getDescription())
                .clickLocation(file.getClickLocation())
                .clickTime(file.getClickTime())
                .occasion(file.getOccasion())
                .uploadedBy(file.getUploadedBy().getId())
                .groupId(file.getGroup().getId())
                .bundleId(file.getBundle() != null ? file.getBundle().getId() : null)
                .build();
    }
}
