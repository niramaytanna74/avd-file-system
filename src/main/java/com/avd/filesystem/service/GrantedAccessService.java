package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.GrantedAccessDto;
import java.util.List;

public interface GrantedAccessService {
    List<GrantedAccessDto> getMyFiles();
    List<GrantedAccessDto> getUserFiles(Long userId);
    void revokeAccess(Long accessId);
}
