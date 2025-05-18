package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.GrantedAccessDto;
import com.avd.filesystem.service.GrantedAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class GrantedAccessController {
    private final GrantedAccessService grantedAccessService;

    @GetMapping("/my-files")
    public ResponseEntity<List<GrantedAccessDto>> getMyFiles() {
        // Implement with SecurityContext
        throw new UnsupportedOperationException("Implement with SecurityContext");
    }

    @GetMapping("/users/{id}/files")
    public ResponseEntity<List<GrantedAccessDto>> getUserFiles(@PathVariable Long id) {
        return ResponseEntity.ok(grantedAccessService.getUserFiles(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeAccess(@PathVariable Long id) {
        grantedAccessService.revokeAccess(id);
        return ResponseEntity.ok().build();
    }
}
