package com.rrhh.backend.application.service;

import com.rrhh.backend.web.dto.login.LoginRequest;
import com.rrhh.backend.web.dto.login.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);
}
