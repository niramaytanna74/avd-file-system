package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByRequestorIdAndFileId(Long requestorId, Long fileId);
    List<AccessRequest> findAll();
}
