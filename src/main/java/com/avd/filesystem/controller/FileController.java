package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.FileDto;
import com.avd.filesystem.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("mode") String mode,
                                        @RequestParam("groupId") Long groupId,
                                        @RequestBody FileDto fileDto,
                                        @RequestParam(value = "bundleName", required = false) String bundleName) {
        if ("SINGLE".equalsIgnoreCase(mode)) {
            return ResponseEntity.ok(fileService.uploadFile(file, fileDto, groupId));
        } else if ("BUNDLE".equalsIgnoreCase(mode)) {
            // For bundle, expect multiple files
            throw new UnsupportedOperationException("Bundle upload should use a different endpoint or logic");
        }
        return ResponseEntity.badRequest().body("Invalid mode");
    }

    @PostMapping("/upload/bundle")
    public ResponseEntity<List<FileDto>> uploadBundle(@RequestParam("files") List<MultipartFile> files,
                                                     @RequestParam("groupId") Long groupId,
                                                     @RequestBody FileDto fileDto,
                                                     @RequestParam("bundleName") String bundleName) {
        return ResponseEntity.ok(fileService.uploadBundle(files, fileDto, groupId, bundleName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFile(@PathVariable Long id) {
        FileDto file = fileService.getFile(id);
        if (file == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(file);
    }

    @GetMapping
    public ResponseEntity<List<FileDto>> getFiles(@RequestParam(required = false) Long uploadedBy,
                                                  @RequestParam(required = false) Long groupId) {
        return ResponseEntity.ok(fileService.getFiles(uploadedBy, groupId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        // 1. Get current user (from SecurityContext)
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        // 2. Check if user has access to the file (direct, bundle, admin, or superadmin)
        boolean hasAccess = fileService.userHasAccessToFile(username, id);
        if (!hasAccess) {
            return ResponseEntity.status(403).body("You do not have access to this file.");
        }
        // 3. If access, return file as attachment
        java.io.File file = fileService.getPhysicalFile(id);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }
        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
