package com.avd.filesystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequestDto {
    private Long id;
    private Long requestorId;
    private Long fileId;
    private String status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
}
