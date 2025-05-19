package com.avd.filesystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime clickTime;
    private String occasion;
    private Long uploadedBy;
    private Long userGroupId;
    private Long bundleId;
}
