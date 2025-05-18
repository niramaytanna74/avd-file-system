package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        // Implement with SecurityContextHolder
        throw new UnsupportedOperationException("Implement with SecurityContext");
    }

    @GetMapping("/groups/{groupId}/users")
    public ResponseEntity<List<UserDto>> getUsersByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(userService.getUsersByGroup(groupId));
    }
}
