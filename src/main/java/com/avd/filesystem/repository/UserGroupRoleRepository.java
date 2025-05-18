package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.UserGroupRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRoleRepository extends JpaRepository<UserGroupRole, Long> {
}
