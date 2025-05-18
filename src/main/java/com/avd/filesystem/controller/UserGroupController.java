package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.UserGroupDto;
import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user-groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserGroupService userGroupService;

    @PostMapping
    public ResponseEntity<UserGroupDto> createUserGroup(@RequestBody UserGroupDto userGroupDto) {
        // Only superadmin should be allowed
        return ResponseEntity.ok(userGroupService.createUserGroup(userGroupDto));
    }

    @PostMapping("/{id}/assign-admin")
    public ResponseEntity<UserDto> assignAdmin(@PathVariable Long id, @RequestParam Long userId) {
        UserDto adminDto = userGroupService.assignAdmin(id, userId);
        return ResponseEntity.ok(adminDto);
    }

    @GetMapping
    public ResponseEntity<List<UserGroupDto>> getAllUserGroups() {
        return ResponseEntity.ok(userGroupService.getAllUserGroups());
    }

    @GetMapping("/{id}/admin")
    public ResponseEntity<UserDto> getGroupAdmin(@PathVariable Long id) {
        // Find the admin for the group
        return userGroupService.getAdminForGroup(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/admins")
    public ResponseEntity<List<UserDto>> getGroupAdmins(@PathVariable Long id) {
        return ResponseEntity.ok(userGroupService.getAdminsForGroup(id));
    }
}
