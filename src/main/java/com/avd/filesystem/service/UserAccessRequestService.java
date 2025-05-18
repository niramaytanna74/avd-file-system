package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.UserAccessRequestDto;
import java.util.List;

public interface UserAccessRequestService {
    UserAccessRequestDto createRequest(Long requestorId, Long userGroupId);
    List<UserAccessRequestDto> getRequestsForUser(Long userId);
    List<UserAccessRequestDto> getRequestsForGroup(Long userGroupId);
    List<UserAccessRequestDto> getPendingRequestsForGroup(Long userGroupId);
    UserAccessRequestDto reviewRequest(Long requestId, Long adminId, String status);
}
