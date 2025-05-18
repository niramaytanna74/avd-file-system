package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.UserAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAccessRequestRepository extends JpaRepository<UserAccessRequest, Long> {
    List<UserAccessRequest> findByRequestorId(Long requestorId);
    List<UserAccessRequest> findByUserGroupId(Long userGroupId);
    List<UserAccessRequest> findByStatus(UserAccessRequest.Status status);
}
