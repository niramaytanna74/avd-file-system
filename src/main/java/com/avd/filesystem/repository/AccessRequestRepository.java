package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
}
