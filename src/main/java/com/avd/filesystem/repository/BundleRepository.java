package com.avd.filesystem.repository;

import com.avd.filesystem.model.entity.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleRepository extends JpaRepository<Bundle, Long> {
}
