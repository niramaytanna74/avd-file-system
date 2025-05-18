package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.GrantedAccessDto;
import com.avd.filesystem.model.entity.GrantedAccess;
import com.avd.filesystem.repository.GrantedAccessRepository;
import com.avd.filesystem.service.GrantedAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GrantedAccessServiceImpl implements GrantedAccessService {
    private final GrantedAccessRepository grantedAccessRepository;

    @Override
    public List<GrantedAccessDto> getMyFiles() {
        // Implement with SecurityContext
        throw new UnsupportedOperationException("Implement with SecurityContext");
    }

    @Override
    public List<GrantedAccessDto> getUserFiles(Long userId) {
        return grantedAccessRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void revokeAccess(Long accessId) {
        grantedAccessRepository.deleteById(accessId);
    }

    private GrantedAccessDto toDto(GrantedAccess access) {
        return GrantedAccessDto.builder()
                .id(access.getId())
                .userId(access.getUser().getId())
                .fileId(access.getFile() != null ? access.getFile().getId() : null)
                .bundleId(access.getBundle() != null ? access.getBundle().getId() : null)
                .accessType(access.getAccessType().name())
                .grantedBy(access.getGrantedBy().getId())
                .grantedAt(access.getGrantedAt())
                .build();
    }
}
