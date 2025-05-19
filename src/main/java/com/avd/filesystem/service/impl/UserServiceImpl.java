package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.model.entity.UserGroupRole;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.repository.UserGroupRoleRepository;
import com.avd.filesystem.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.util.CollectionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto, String rawPassword) {
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(User.Role.valueOf(userDto.getRole()))
                .build();
        user = userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public UserDto getCurrentUser() {
        // Implement with SecurityContextHolder in controller layer
        throw new UnsupportedOperationException("Implement in controller with SecurityContext");
    }

    @Override
    public List<UserDto> getUsersByGroup(Long groupId) {
        List<UserGroupRole> roles = userGroupRoleRepository.findAll();
        return roles.stream()
                .filter(r -> r.getUserGroup().getId().equals(groupId))
                .map(r -> toDto(r.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        // Convert UserGroupRole to Set<Long> for groupIds
        if(CollectionUtils.isEmpty(user.getUserGroupRoles())) {
            return UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .groupIds(Set.of())
                    .build();
        }
        Set<Long> groupIds = user.getUserGroupRoles().stream()
                .map(r -> r.getUserGroup().getId())
                .collect(Collectors.toSet());
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .groupIds(groupIds)
                .build();
    }
}
