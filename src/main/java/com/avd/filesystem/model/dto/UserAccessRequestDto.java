package com.avd.filesystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessRequestDto {
    private Long id;
    private Long requestorId;
    private Long userGroupId;
    private String status;
    private Long reviewedById;
    private LocalDateTime reviewedAt;
}
