package com.avd.filesystem.service;

import com.avd.filesystem.model.dto.BundleDto;
import java.util.List;

public interface BundleService {
    BundleDto getBundle(Long id);
    List<BundleDto> getAllBundles();
}
