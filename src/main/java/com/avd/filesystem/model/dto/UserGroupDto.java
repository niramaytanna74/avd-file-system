package com.avd.filesystem.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupDto {
    private Long id;
    private String name;
    private String description;
}
