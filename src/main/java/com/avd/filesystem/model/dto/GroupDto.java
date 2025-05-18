package com.avd.filesystem.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {
    private Long id;
    private String name;
    private String description;
}
