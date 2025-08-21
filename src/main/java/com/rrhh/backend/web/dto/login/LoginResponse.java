package com.rrhh.backend.web.dto.login;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse{
    private String token;
    private String username;
    private String fullName;
    private List<String> roles;
    private Long expiredAt;
}
