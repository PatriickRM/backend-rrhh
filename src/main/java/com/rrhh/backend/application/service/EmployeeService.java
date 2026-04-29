package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.employee.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EmployeeService {

    EmployeeResponseDTO create(EmployeeRequestDTO dto);
    EmployeeResponseDTO update(Long id, EmployeeUpdateDTO dto);
    EmployeeResponseDTO updateStatusToRetired(Long id);
    EmployeeResponseDTO getById(Long id);

    // ── Paginados ─────────────────────────────────────────────────────────
    PagedResponse<EmployeeResponseDTO> getAllEmployees(Pageable pageable);
    PagedResponse<EmployeeResponseDTO> searchByName(String name, Pageable pageable);
    PagedResponse<EmployeeResponseDTO> getByDepartment(Long departmentId, Pageable pageable);
    PagedResponse<EmployeeResponseDTO> getByPosition(Long positionId, Pageable pageable);
    PagedResponse<EmployeeResponseDTO> getByHireDateRange(LocalDate start, LocalDate end, Pageable pageable);
    PagedResponse<EmployeeResponseDTO> filterByStatus(EmployeeStatus status, Pageable pageable);

    // ── Dashboard (sin paginar — datos propios del empleado autenticado) ──
    EmployeeDashboardDTO getEmployeeDashboard(String username);
    EmployeeStatsDTO getEmployeeStats(String username);
}
