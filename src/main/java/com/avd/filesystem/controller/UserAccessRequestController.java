package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.UserAccessRequestDto;
import com.avd.filesystem.service.UserAccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
@RequiredArgsConstructor
public class UserAccessRequestController {
    private final UserAccessRequestService userAccessRequestService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAccessRequestDto> createRequest(@RequestParam Long groupId, @RequestParam Long userId) {
        return ResponseEntity.ok(userAccessRequestService.createRequest(userId, groupId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserAccessRequestDto>> getRequestsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userAccessRequestService.getRequestsForUser(userId));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserAccessRequestDto>> getRequestsForGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(userAccessRequestService.getRequestsForGroup(groupId));
    }

    @GetMapping("/group/{groupId}/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserAccessRequestDto>> getPendingRequestsForGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(userAccessRequestService.getPendingRequestsForGroup(groupId));
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<UserAccessRequestDto> approveRequest(@PathVariable Long requestId, @RequestParam Long adminId) {
        return ResponseEntity.ok(userAccessRequestService.reviewRequest(requestId, adminId, "APPROVED"));
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<UserAccessRequestDto> rejectRequest(@PathVariable Long requestId, @RequestParam Long adminId) {
        return ResponseEntity.ok(userAccessRequestService.reviewRequest(requestId, adminId, "REJECTED"));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<UserAccessRequestDto> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.notFound().build();
    }
}
