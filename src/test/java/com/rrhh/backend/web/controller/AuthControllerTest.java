package com.rrhh.backend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.service.AuthService;
import com.rrhh.backend.security.CustomUserDetailsService;
import com.rrhh.backend.security.filter.JwtAuthenticationFilter;
import com.rrhh.backend.security.util.JwtUtil;
import com.rrhh.backend.web.dto.login.LoginRequest;
import com.rrhh.backend.web.dto.login.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController - Tests de capa web")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("debe retornar 200 y el token cuando las credenciales son válidas")
        void debe_retornar_token_con_credenciales_validas() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("admin")
                    .password("password123")
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .token("eyJhbGciOiJIUzI1NiJ9.mock_token")
                    .username("admin")
                    .fullName("Administrador RRHH")
                    .roles(List.of("ROLE_CHRO"))
                    .expiredAt(System.currentTimeMillis() + 3600000L)
                    .build();

            when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.username").value("admin"))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_CHRO"));
        }

        @Test
        @DisplayName("debe retornar 400 cuando las credenciales son incorrectas")
        void debe_retornar_400_con_credenciales_invalidas() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .username("usuario_invalido")
                    .password("clave_erronea")
                    .build();

            when(authService.authenticate(any(LoginRequest.class)))
                    .thenThrow(new ErrorSistema("Usuario o contraseña incorrectos"));

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
        }
    }
}
