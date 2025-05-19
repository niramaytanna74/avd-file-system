package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(userService.getUserById(user.getId())))
                .orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/user-groups/{userGroupId}/users")
    public ResponseEntity<List<UserDto>> getUsersByUserGroup(@PathVariable Long userGroupId) {
        return ResponseEntity.ok(userService.getUsersByGroup(userGroupId));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsersByRole(@RequestParam(value = "role", required = false) String role) {
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            // Filter users by ADMIN role
            List<UserDto> admins = userService.getAllUsers().stream()
                .filter(u -> u.getRole().equalsIgnoreCase("ADMIN"))
                .toList();
            return ResponseEntity.ok(admins);
        }
        // Optionally, return all users if no role param
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        UserDto updated = userService.updateUserRole(id, role);
        return ResponseEntity.ok(updated);
    }
}
