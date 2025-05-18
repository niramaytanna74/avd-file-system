package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.GrantedAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrantedAccessRepository extends JpaRepository<GrantedAccess, Long> {
}
