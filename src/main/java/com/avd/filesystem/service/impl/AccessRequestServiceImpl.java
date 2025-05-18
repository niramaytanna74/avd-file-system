
package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.AccessRequestDto;
import com.avd.filesystem.model.entity.AccessRequest;
import com.avd.filesystem.model.entity.File;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.repository.AccessRequestRepository;
import com.avd.filesystem.repository.FileRepository;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccessRequestServiceImpl implements AccessRequestService {
    private final AccessRequestRepository accessRequestRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    public AccessRequestDto requestAccess(Long fileId) {
        File file = fileRepository.findById(fileId).orElseThrow();
        // Assume current user is fetched from SecurityContext
        throw new UnsupportedOperationException("Implement with SecurityContext");
    }

    @Override
    public List<AccessRequestDto> getAccessRequestsForAdmin(Long groupId) {
        return accessRequestRepository.findAll().stream()
                .filter(r -> r.getFile().getGroup().getId().equals(groupId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccessRequestDto approveRequest(Long requestId) {
        AccessRequest req = accessRequestRepository.findById(requestId).orElseThrow();
        req.setStatus(AccessRequest.Status.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        // req.setReviewedBy(currentAdmin);
        accessRequestRepository.save(req);
        return toDto(req);
    }

    @Override
    public AccessRequestDto rejectRequest(Long requestId) {
        AccessRequest req = accessRequestRepository.findById(requestId).orElseThrow();
        req.setStatus(AccessRequest.Status.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        // req.setReviewedBy(currentAdmin);
        accessRequestRepository.save(req);
        return toDto(req);
    }

    private AccessRequestDto toDto(AccessRequest req) {
        return AccessRequestDto.builder()
                .id(req.getId())
                .requestorId(req.getRequestor().getId())
                .fileId(req.getFile().getId())
                .status(req.getStatus().name())
                .reviewedBy(req.getReviewedBy() != null ? req.getReviewedBy().getId() : null)
                .reviewedAt(req.getReviewedAt())
                .build();
    }
}
