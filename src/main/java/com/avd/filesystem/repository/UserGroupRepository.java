package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    boolean existsByName(String name);
}
