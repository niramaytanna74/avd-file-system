package com.avd.filesystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantedAccessDto {
    private Long id;
    private Long userId;
    private Long fileId;
    private Long bundleId;
    private String accessType;
    private Long grantedBy;
    private LocalDateTime grantedAt;
}
