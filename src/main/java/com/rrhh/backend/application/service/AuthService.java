package com.rrhh.backend.application.service;

import com.rrhh.backend.web.dto.LoginRequest;
import com.rrhh.backend.web.dto.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);
}
