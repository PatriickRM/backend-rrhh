package com.rrhh.backend.web.dto.user;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String userName;
    private String fullName;
    private boolean enabled;
    private Set<String> roles;
}
