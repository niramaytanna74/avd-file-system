package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.AccessRequestDto;
import java.util.List;

public interface AccessRequestService {
    AccessRequestDto requestAccess(Long fileId);
    List<AccessRequestDto> getAccessRequestsForAdmin(Long groupId);
    AccessRequestDto approveRequest(Long requestId);
    AccessRequestDto rejectRequest(Long requestId);
    List<AccessRequestDto> getAllAccessRequests();
    List<AccessRequestDto> getAccessRequestsByFileIdAndRequestorId(Long fileId, Long requestorId);
}
