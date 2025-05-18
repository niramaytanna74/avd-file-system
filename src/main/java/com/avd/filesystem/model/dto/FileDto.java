package com.avd.filesystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    private Long id;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String description;
    private String clickLocation;
    private LocalDateTime clickTime;
    private String occasion;
    private Long uploadedBy;
    private Long groupId;
    private Long bundleId;
}
