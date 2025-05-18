package com.avd.filesystem.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupRoleDto {
    private Long id;
    private Long userId;
    private Long groupId;
    private String role;
}
