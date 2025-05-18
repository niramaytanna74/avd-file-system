package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.GroupDto;
import java.util.List;

public interface GroupService {
    GroupDto createGroup(GroupDto groupDto);
    void assignAdmin(Long groupId, Long userId);
    List<GroupDto> getAllGroups();
}
