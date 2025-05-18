package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.UserDto;

import com.avd.filesystem.service.UserService;

import com.avd.filesystem.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto, @RequestParam String password) {
        return ResponseEntity.ok(userService.registerUser(userDto, password));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        // JWT authentication logic
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        String token = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
