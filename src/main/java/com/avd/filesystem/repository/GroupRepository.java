package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByName(String name);
}
