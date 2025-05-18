package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.UserAccessRequestDto;
import com.avd.filesystem.model.entity.UserAccessRequest;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.model.entity.UserGroup;
import com.avd.filesystem.model.entity.UserGroupRole;
import com.avd.filesystem.repository.UserAccessRequestRepository;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.repository.UserGroupRepository;
import com.avd.filesystem.repository.UserGroupRoleRepository;
import com.avd.filesystem.service.UserAccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccessRequestServiceImpl implements UserAccessRequestService {
    private final UserAccessRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;

    @Override
    public UserAccessRequestDto createRequest(Long requestorId, Long userGroupId) {
        User requestor = userRepository.findById(requestorId).orElseThrow();
        UserGroup group = userGroupRepository.findById(userGroupId).orElseThrow();
        UserAccessRequest request = UserAccessRequest.builder()
                .requestor(requestor)
                .userGroup(group)
                .status(UserAccessRequest.Status.PENDING)
                .build();
        request = requestRepository.save(request);
        return toDto(request);
    }

    @Override
    public List<UserAccessRequestDto> getRequestsForUser(Long userId) {
        return requestRepository.findByRequestorId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<UserAccessRequestDto> getRequestsForGroup(Long userGroupId) {
        return requestRepository.findByUserGroupId(userGroupId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<UserAccessRequestDto> getPendingRequestsForGroup(Long userGroupId) {
        return requestRepository.findByUserGroupId(userGroupId).stream()
                .filter(r -> r.getStatus() == UserAccessRequest.Status.PENDING)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserAccessRequestDto reviewRequest(Long requestId, Long adminId, String status) {
        UserAccessRequest request = requestRepository.findById(requestId).orElseThrow();
        User admin = userRepository.findById(adminId).orElseThrow();
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        request.setStatus(UserAccessRequest.Status.valueOf(status));
        request = requestRepository.save(request);
        // If approved, add user to group (create UserGroupRole)
        if (request.getStatus() == UserAccessRequest.Status.APPROVED) {
            UserGroupRole ugr = UserGroupRole.builder()
                .user(request.getRequestor())
                .userGroup(request.getUserGroup())
                .role(UserGroupRole.Role.USER)
                .build();
            userGroupRoleRepository.save(ugr);
        }
        return toDto(request);
    }

    private UserAccessRequestDto toDto(UserAccessRequest req) {
        return UserAccessRequestDto.builder()
                .id(req.getId())
                .requestorId(req.getRequestor().getId())
                .userGroupId(req.getUserGroup().getId())
                .status(req.getStatus().name())
                .reviewedById(req.getReviewedBy() != null ? req.getReviewedBy().getId() : null)
                .reviewedAt(req.getReviewedAt())
                .build();
    }
}
