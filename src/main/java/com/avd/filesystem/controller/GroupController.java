package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.GroupDto;
import com.avd.filesystem.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@RequestBody GroupDto groupDto) {
        // Only superadmin should be allowed
        return ResponseEntity.ok(groupService.createGroup(groupDto));
    }

    @PostMapping("/{id}/assign-admin")
    public ResponseEntity<Void> assignAdmin(@PathVariable Long id, @RequestParam Long userId) {
        groupService.assignAdmin(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }
}
