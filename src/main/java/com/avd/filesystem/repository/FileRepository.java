package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
