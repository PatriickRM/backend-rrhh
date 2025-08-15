package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.service.AuthService;
import com.rrhh.backend.security.CustomUserDetails;
import com.rrhh.backend.security.util.JwtUtil;
import com.rrhh.backend.web.dto.LoginRequest;
import com.rrhh.backend.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    @Override
    public LoginResponse authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new ErrorSistema("Usuario o contraseña incorrectos");
        }

        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(user);
        Long expireAt = jwtUtil.extractExpiration(token).getTime();
        CustomUserDetails customUser = (CustomUserDetails) user;

        return LoginResponse.builder().token(token).username(user.getUsername()).fullName(customUser.getFullName())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .expiredAt(expireAt)
                .build();
    }
}
