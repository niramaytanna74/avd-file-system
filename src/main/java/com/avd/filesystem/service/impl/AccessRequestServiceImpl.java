package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.AccessRequestDto;
import com.avd.filesystem.model.entity.AccessRequest;
import com.avd.filesystem.model.entity.File;
import com.avd.filesystem.model.entity.User;
import com.avd.filesystem.repository.AccessRequestRepository;
import com.avd.filesystem.repository.FileRepository;
import com.avd.filesystem.repository.UserRepository;
import com.avd.filesystem.repository.UserGroupRoleRepository;
import com.avd.filesystem.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccessRequestServiceImpl implements AccessRequestService {
    private static final Logger log = LoggerFactory.getLogger(AccessRequestServiceImpl.class);
    private final AccessRequestRepository accessRequestRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;

    @Override
    public AccessRequestDto requestAccess(Long fileId) {
        log.info("Requesting access for fileId: {}", fileId);
        File file = fileRepository.findById(fileId).orElseThrow();
        // Get current authenticated user
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("Request access failed: Not authenticated");
            throw new SecurityException("Not authenticated");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        log.info("User '{}' is requesting access to file {}", username, fileId);
        // Check if a pending request already exists
        boolean alreadyRequested = accessRequestRepository.findAll().stream()
            .anyMatch(r -> r.getFile().getId().equals(fileId)
                && r.getRequestor().getId().equals(user.getId())
                && r.getStatus() == AccessRequest.Status.PENDING);
        if (alreadyRequested) {
            log.warn("User '{}' already has a pending request for file {}", username, fileId);
            throw new IllegalStateException("You have already requested access to this file.");
        }
        AccessRequest request = AccessRequest.builder()
                .file(file)
                .requestor(user)
                .status(AccessRequest.Status.PENDING)
                .build();
        accessRequestRepository.save(request);
        log.info("Access request created: user={}, fileId={}, requestId={}", username, fileId, request.getId());
        return toDto(request);
    }

    @Override
    public List<AccessRequestDto> getAccessRequestsForAdmin(Long groupId) {
        log.info("Fetching access requests for groupId: {}", groupId);
        return accessRequestRepository.findAll().stream()
                .filter(r -> r.getFile().getUserGroup().getId().equals(groupId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccessRequestDto approveRequest(Long requestId) {
        log.info("Approving access request: {}", requestId);
        AccessRequest req = accessRequestRepository.findById(requestId).orElseThrow();
        // Get current authenticated user
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("Approve request failed: Not authenticated");
            throw new SecurityException("Not authenticated");
        }
        String username = authentication.getName();
        User admin = userRepository.findByUsername(username).orElseThrow();
        boolean isSuperadmin = admin.getRole() == User.Role.SUPERADMIN;
        boolean isGroupAdmin = userGroupRoleRepository.findAll().stream()
            .anyMatch(r -> r.getUser().getId().equals(admin.getId()) &&
                          r.getUserGroup().getId().equals(req.getFile().getUserGroup().getId()) &&
                          r.getRole() == com.avd.filesystem.model.entity.UserGroupRole.Role.ADMIN);
        if (!isSuperadmin && !isGroupAdmin) {
            log.warn("User '{}' is not authorized to approve request {}", username, requestId);
            throw new SecurityException("Only group admin or superadmin can approve requests");
        }
        req.setStatus(AccessRequest.Status.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        req.setReviewedBy(admin);
        accessRequestRepository.save(req);
        log.info("Access request {} approved by user '{}'", requestId, username);
        return toDto(req);
    }

    @Override
    public AccessRequestDto rejectRequest(Long requestId) {
        log.info("Rejecting access request: {}", requestId);
        AccessRequest req = accessRequestRepository.findById(requestId).orElseThrow();
        // Get current authenticated user
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("Reject request failed: Not authenticated");
            throw new SecurityException("Not authenticated");
        }
        String username = authentication.getName();
        User admin = userRepository.findByUsername(username).orElseThrow();
        boolean isSuperadmin = admin.getRole() == User.Role.SUPERADMIN;
        boolean isGroupAdmin = userGroupRoleRepository.findAll().stream()
            .anyMatch(r -> r.getUser().getId().equals(admin.getId()) &&
                          r.getUserGroup().getId().equals(req.getFile().getUserGroup().getId()) &&
                          r.getRole() == com.avd.filesystem.model.entity.UserGroupRole.Role.ADMIN);
        if (!isSuperadmin && !isGroupAdmin) {
            log.warn("User '{}' is not authorized to reject request {}", username, requestId);
            throw new SecurityException("Only group admin or superadmin can reject requests");
        }
        req.setStatus(AccessRequest.Status.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        req.setReviewedBy(admin);
        accessRequestRepository.save(req);
        log.info("Access request {} rejected by user '{}'", requestId, username);
        return toDto(req);
    }

    @Override
    public List<AccessRequestDto> getAllAccessRequests() {
        log.info("Fetching all access requests (superadmin)");
        return accessRequestRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRequestDto> getAccessRequestsByFileIdAndRequestorId(Long fileId, Long requestorId) {
        log.info("Fetching access requests for fileId: {} and requestorId: {}", fileId, requestorId);
        return accessRequestRepository.findByRequestorIdAndFileId(requestorId, fileId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
