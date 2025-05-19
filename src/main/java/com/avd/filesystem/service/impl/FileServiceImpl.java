package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.FileDto;
import com.avd.filesystem.model.entity.*;
import com.avd.filesystem.repository.*;
import com.avd.filesystem.service.FileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    private final FileRepository fileRepository;
    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BundleRepository bundleRepository;
    private final GrantedAccessRepository grantedAccessRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final String fileBasePath = "uploads"; // Should be configurable

    @Override
    public FileDto uploadFile(MultipartFile file, FileDto fileDto, Long groupId) {
        log.info("uploadFile called: file={}, fileDto={}, groupId={}", file != null ? file.getOriginalFilename() : null, fileDto, groupId);
        UserGroup group = groupRepository.findById(groupId).orElseThrow();
        log.info("FileDto: {}", fileDto.getUploadedBy());
        User uploader = userRepository.findById(fileDto.getUploadedBy()).orElseThrow();
        // Allow SUPERADMIN to upload to any group
        if (uploader.getRole() != User.Role.SUPERADMIN) {
            boolean isMember = uploader.getUserGroupRoles().stream()
                .anyMatch(role -> role.getUserGroup().getId().equals(groupId));
            if (!isMember) {
                log.warn("User {} is not a member of group {}", uploader.getId(), groupId);
                throw new IllegalArgumentException("User is not a member of the selected group");
            }
        }
        String filePath = storeFile(file, group.getName(), uploader.getUsername());
        log.info("File stored at: {}", filePath);
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
                .userGroup(group)
                .build();
        entity = fileRepository.save(entity);
        log.info("File entity saved: {}", entity.getId());
        return toDto(entity);
    }

    @Override
    public List<FileDto> uploadBundle(List<MultipartFile> files, FileDto fileDto, Long groupId, String bundleName) {
        log.info("uploadBundle called: files={}, fileDto={}, groupId={}, bundleName={}", files != null ? files.size() : 0, fileDto, groupId, bundleName);
        UserGroup group = groupRepository.findById(groupId).orElseThrow();
        User uploader = userRepository.findById(fileDto.getUploadedBy()).orElseThrow();
        Bundle bundle = Bundle.builder()
                .name(bundleName)
                .uploadedBy(uploader)
                .userGroup(group)
                .build();
        final Bundle savedBundle = bundleRepository.save(bundle);
        List<File> fileEntities = files.stream().map(f -> {
            String filePath = storeFile(f, group.getName(), uploader.getUsername());
            log.info("Bundle file stored at: {}", filePath);
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
                    .userGroup(group)
                    .bundle(savedBundle)
                    .build();
        }).collect(Collectors.toList());
        fileRepository.saveAll(fileEntities);
        log.info("Bundle files saved: {}", fileEntities.size());
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
                             (groupId == null || f.getUserGroup().getId().equals(groupId)))
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
                .anyMatch(r -> r.getUserGroup().getId().equals(file.getUserGroup().getId()) && r.getRole() == UserGroupRole.Role.ADMIN);
        if (isAdmin) return true;
        // 6. Check granted access (file or bundle)
        // (a) Direct file access
        List<AccessRequest> accessRequests = accessRequestRepository.findByRequestorIdAndFileId(user.getId(), fileId);
        if (accessRequests == null || accessRequests.isEmpty()) {
            return false;
        }
        boolean hasFileAccess = accessRequests.stream()
                .anyMatch(a -> a.getFile() != null && a.getFile().getId().equals(fileId) && a.getStatus() == AccessRequest.Status.APPROVED);
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

    private String storeFile(MultipartFile file, String groupName, String username) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "upload";
            }
            String baseName = originalName;
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) {
                baseName = originalName.substring(0, dot);
                ext = originalName.substring(dot);
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uniqueName = baseName + "_" + timestamp + ext;
            Path dir = Paths.get(fileBasePath, groupName, username);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path filePath = dir.resolve(uniqueName);
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
                .userGroupId(file.getUserGroup().getId())
                .bundleId(file.getBundle() != null ? file.getBundle().getId() : null)
                .build();
    }
}
