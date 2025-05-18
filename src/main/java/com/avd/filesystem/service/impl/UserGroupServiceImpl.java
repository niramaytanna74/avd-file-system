package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.UserGroupDto;
import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.model.entity.UserGroup;
import com.avd.filesystem.model.entity.UserGroupRole;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.repository.UserGroupRepository;
import com.avd.filesystem.repository.UserGroupRoleRepository;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserGroupServiceImpl implements UserGroupService {
    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private UserGroupRoleRepository userGroupRoleRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserGroupDto createUserGroup(UserGroupDto userGroupDto) {
        UserGroup userGroup = UserGroup.builder()
                .name(userGroupDto.getName())
                .description(userGroupDto.getDescription())
                .build();
        userGroup = userGroupRepository.save(userGroup);
        return toDto(userGroup);
    }

    @Override
    public UserDto assignAdmin(Long userGroupId, Long userId) {
        // Only SUPERADMIN can assign admin
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != User.Role.SUPERADMIN) {
            throw new SecurityException("Only superadmin can assign group admin");
        }
        UserGroup group = userGroupRepository.findById(userGroupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Selected user is not an ADMIN");
        }
        // Remove existing admin for this group
        userGroupRoleRepository.findAll().stream()
            .filter(r -> r.getUserGroup().getId().equals(userGroupId) && r.getRole() == UserGroupRole.Role.ADMIN)
            .forEach(r -> userGroupRoleRepository.deleteById(r.getId()));
        // Assign new admin
        UserGroupRole newAdminRole = UserGroupRole.builder()
            .user(user)
            .userGroup(group)
            .role(UserGroupRole.Role.ADMIN)
            .build();
        userGroupRoleRepository.save(newAdminRole);
        return toUserDto(user);
    }

    @Override
    public List<UserGroupDto> getAllUserGroups() {
        return userGroupRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getAdminForGroup(Long userGroupId) {
        return userGroupRoleRepository.findAll().stream()
            .filter(r -> r.getUserGroup().getId().equals(userGroupId) && r.getRole() == UserGroupRole.Role.ADMIN)
            .findFirst()
            .map(r -> toUserDto(r.getUser()));
    }

    @Override
    public List<UserDto> getAdminsForGroup(Long userGroupId) {
        return userGroupRoleRepository.findAll().stream()
            .filter(r -> r.getUserGroup().getId().equals(userGroupId) && r.getRole() == UserGroupRole.Role.ADMIN)
            .map(r -> toUserDto(r.getUser()))
            .collect(Collectors.toList());
    }

    private User getCurrentAuthenticatedUser() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private UserGroupDto toDto(UserGroup userGroup) {
        return UserGroupDto.builder()
                .id(userGroup.getId())
                .name(userGroup.getName())
                .description(userGroup.getDescription())
                .build();
    }

    private UserDto toUserDto(User user) {
        Set<Long> groupIds = user.getUserGroupRoles() == null ? null :
            user.getUserGroupRoles().stream().map(role -> role.getUserGroup().getId()).collect(Collectors.toSet());
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole().name())
            .groupIds(groupIds)
            .build();
    }
}
