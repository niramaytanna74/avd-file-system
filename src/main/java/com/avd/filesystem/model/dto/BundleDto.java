package com.avd.filesystem.model.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleDto {
    private Long id;
    private String name;
    private Long uploadedBy;
    private Long groupId;
    private Set<Long> fileIds;
}
