package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import com.rrhh.backend.security.CustomUserDetails;
import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;
import com.rrhh.backend.web.dto.leave.LeaveRequestCreateDTO;
import com.rrhh.backend.web.dto.leave.LeaveRequestEmployeeDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-requests")
@AllArgsConstructor
public class EmployeeLeaveController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LeaveRequestCreateDTO> createLeaveRequest(
            @RequestPart("request") LeaveRequestCreateDTO dto,
            @RequestPart(value = "evidence", required = false) MultipartFile evidenceFile,
            Authentication authentication) {

        //Obtener id de la autenticación
        Long employeeId = extractEmployeeIdFromAuth(authentication);

        LeaveRequestCreateDTO result = leaveRequestService.createLeaveRequest(dto, employeeId, evidenceFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestEmployeeDTO>> getMyLeaveRequests(Authentication authentication) {
        Long employeeId = extractEmployeeIdFromAuth(authentication);
        List<LeaveRequestEmployeeDTO> requests = leaveRequestService.getMyLeaveRequests(employeeId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<LeaveRequestEmployeeDTO> getLeaveRequestDetail(
            @PathVariable Long requestId,
            Authentication authentication) {
        Long employeeId = extractEmployeeIdFromAuth(authentication);
        LeaveRequestEmployeeDTO request = leaveRequestService.getMyLeaveRequestDetail(requestId, employeeId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceDTO> getMyLeaveBalance(Authentication authentication) {
        Long employeeId = extractEmployeeIdFromAuth(authentication);
        LeaveBalanceDTO balance = leaveRequestService.getEmployeeLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }


    private Long extractEmployeeIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ErrorSistema("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            // Obtener el User ID del token
            Long userId = customUserDetails.getId();

            // Buscar el empleado asociado a este usuario
            Employee employee = employeeRepository.findByUserId(userId)
                    .orElseThrow(() -> new ErrorSistema("No se encontró empleado asociado al usuario"));

            return employee.getId();
        }

        throw new ErrorSistema("Token de autenticación inválido");
    }

}
