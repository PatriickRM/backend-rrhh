package com.rrhh.backend.security;

import com.rrhh.backend.security.custom.CustomAccessDeniedHandler;
import com.rrhh.backend.security.custom.CustomAuthEntryPoint;
import com.rrhh.backend.security.filter.JwtAuthenticationFilter;
import com.rrhh.backend.security.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;          // ← nuevo
    private final CustomUserDetailsService userService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",   // Angular dev
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── Públicos ──────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        // ── Swagger UI ────────────────────────────────────────
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // ── Actuator ──────────────────────────────────────────
                        .requestMatchers("/actuator/health").permitAll()
                        // ── Empleado autenticado ──────────────────────────────
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "HEAD")
                        // ── Jefe de departamento ──────────────────────────────
                        .requestMatchers("/api/head/**").hasRole("HEAD")
                        // ── RRHH / CHRO ───────────────────────────────────────
                        .requestMatchers("/api/chro/**").hasRole("CHRO")
                        .requestMatchers(HttpMethod.POST,  "/api/users").hasRole("CHRO")
                        .requestMatchers(HttpMethod.GET,   "/api/users/**").hasRole("CHRO")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("CHRO")
                        .requestMatchers("/api/positions/**").hasRole("CHRO")
                        .requestMatchers("/api/departments/**").hasRole("CHRO")
                        .requestMatchers(HttpMethod.PUT,   "/api/employees/**").hasRole("CHRO")
                        .requestMatchers(HttpMethod.POST,  "/api/employees/**").hasRole("CHRO")
                        .requestMatchers(HttpMethod.GET,   "/api/employees/**").hasRole("CHRO")
                        // ── Reportes ──────────────────────────────────────────
                        .requestMatchers("/api/reports/**").hasRole("CHRO")
                        // ── Resto requiere autenticación ─────────────────────
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthEntryPoint)
                )
                .authenticationProvider(authenticationProvider())
                // RateLimitFilter va ANTES que JwtAuthenticationFilter:
                // limita intentos antes de que se procese el token
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
