package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.service.EmployeeService;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.security.CustomUserDetails;
import com.rrhh.backend.web.dto.employee.EmployeeDashboardDTO;
import com.rrhh.backend.web.dto.employee.EmployeeStatsDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@AllArgsConstructor
public class EmployeeDashboardController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<EmployeeDashboardDTO> getDashboard(Authentication authentication) {
        String username = extractUsernameFromAuth(authentication);
        EmployeeDashboardDTO dashboard = employeeService.getEmployeeDashboard(username);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/stats")
    public ResponseEntity<EmployeeStatsDTO> getStats(Authentication authentication) {
        String username = extractUsernameFromAuth(authentication);
        EmployeeStatsDTO stats = employeeService.getEmployeeStats(username);
        return ResponseEntity.ok(stats);
    }

    private String extractUsernameFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ErrorSistema("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUsername();
        }

        throw new ErrorSistema("Token de autenticación inválido");
    }
}