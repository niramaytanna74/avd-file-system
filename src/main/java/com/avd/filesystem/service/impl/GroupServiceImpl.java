package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.GroupDto;
import com.avd.filesystem.model.entity.Group;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.model.entity.UserGroupRole;
import com.avd.filesystem.repository.GroupRepository;
import com.avd.filesystem.repository.UserGroupRoleRepository;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;

    @Override
    public GroupDto createGroup(GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new IllegalArgumentException("Group name already exists");
        }
        Group group = Group.builder()
                .name(groupDto.getName())
                .description(groupDto.getDescription())
                .build();
        group = groupRepository.save(group);
        return toDto(group);
    }

    @Override
    public void assignAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        UserGroupRole ugr = UserGroupRole.builder()
                .group(group)
                .user(user)
                .role(UserGroupRole.Role.ADMIN)
                .build();
        userGroupRoleRepository.save(ugr);
    }

    @Override
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private GroupDto toDto(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .build();
    }
}
