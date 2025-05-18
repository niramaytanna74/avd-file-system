package com.avd.filesystem.service.impl;

import com.avd.filesystem.model.dto.BundleDto;
import com.avd.filesystem.model.entity.Bundle;
import com.avd.filesystem.repository.BundleRepository;
import com.avd.filesystem.service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BundleServiceImpl implements BundleService {
    private final BundleRepository bundleRepository;

    @Override
    public BundleDto getBundle(Long id) {
        return bundleRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public List<BundleDto> getAllBundles() {
        return bundleRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private BundleDto toDto(Bundle bundle) {
        return BundleDto.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .uploadedBy(bundle.getUploadedBy().getId())
                .groupId(bundle.getUserGroup().getId())
                .fileIds(bundle.getFiles().stream().map(f -> f.getId()).collect(Collectors.toSet()))
                .build();
    }
}
