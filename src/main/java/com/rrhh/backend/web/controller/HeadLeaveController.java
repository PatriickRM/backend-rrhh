package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.application.utils.FileStorageService;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.security.CustomUserDetails;
import com.rrhh.backend.web.dto.leave.LeaveHeadResponseDTO;

import com.rrhh.backend.web.dto.leave.LeaveRequestHeadDTO;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/head/leave-requests")
@AllArgsConstructor
public class HeadLeaveController {
    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestHeadDTO>> getPendingRequests(Authentication authentication) {
        Long headEmployeeId = extractEmployeeIdFromAuth(authentication);
        List<LeaveRequestHeadDTO> requests = leaveRequestService.getPendingRequestsForHead(headEmployeeId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestHeadDTO>> getAllRequests(Authentication authentication) {
        Long headEmployeeId = extractEmployeeIdFromAuth(authentication);
        List<LeaveRequestHeadDTO> requests = leaveRequestService.getAllRequestsForHead(headEmployeeId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestHeadDTO> getRequestDetail(
            @PathVariable Long id,
            Authentication authentication) {

        Long headEmployeeId = extractEmployeeIdFromAuth(authentication);
        LeaveRequestHeadDTO requestDetail = leaveRequestService.getRequestDetailForHead(id, headEmployeeId);
        return ResponseEntity.ok(requestDetail);
    }

    @PutMapping("/respond")
    public ResponseEntity<String> respondToRequest(
            @RequestBody LeaveHeadResponseDTO responseDTO,
            Authentication authentication) {

        Long headEmployeeId = extractEmployeeIdFromAuth(authentication);
        leaveRequestService.respondAsHead(responseDTO, headEmployeeId);
        return ResponseEntity.ok("Respuesta enviada exitosamente");
    }
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
