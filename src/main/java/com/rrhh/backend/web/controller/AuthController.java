package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.AuthService;
import com.rrhh.backend.web.dto.login.LoginRequest;
import com.rrhh.backend.web.dto.login.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.authenticate(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}
