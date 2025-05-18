package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.UserDto;
import com.avd.filesystem.model.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto registerUser(UserDto userDto, String rawPassword);
    UserDto getUserById(Long id);
    UserDto getCurrentUser();
    List<UserDto> getUsersByGroup(Long groupId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<UserDto> getAllUsers();
}
