package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.AccessRequestDto;
import com.avd.filesystem.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class AccessRequestController {
    private final AccessRequestService accessRequestService;

    @PostMapping("/request")
    public ResponseEntity<AccessRequestDto> requestAccess(@RequestParam Long fileId) {
        return ResponseEntity.ok(accessRequestService.requestAccess(fileId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AccessRequestDto>> getAccessRequestsForAdmin(@RequestParam Long groupId) {
        return ResponseEntity.ok(accessRequestService.getAccessRequestsForAdmin(groupId));
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<AccessRequestDto> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(accessRequestService.approveRequest(id));
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<AccessRequestDto> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(accessRequestService.rejectRequest(id));
    }
}
