package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.UserGroupDto;
import com.avd.filesystem.model.dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserGroupService {
    UserGroupDto createUserGroup(UserGroupDto userGroupDto);
    UserDto assignAdmin(Long userGroupId, Long userId);
    List<UserGroupDto> getAllUserGroups();
    Optional<UserDto> getAdminForGroup(Long userGroupId);
    List<UserDto> getAdminsForGroup(Long userGroupId);
}
