package com.avd.filesystem.controller;

import com.avd.filesystem.model.dto.BundleDto;
import com.avd.filesystem.service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class BundleController {
    private final BundleService bundleService;

    @GetMapping("/{id}")
    public ResponseEntity<BundleDto> getBundle(@PathVariable Long id) {
        BundleDto bundle = bundleService.getBundle(id);
        if (bundle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bundle);
    }

    @GetMapping
    public ResponseEntity<List<BundleDto>> getAllBundles() {
        return ResponseEntity.ok(bundleService.getAllBundles());
    }
}
