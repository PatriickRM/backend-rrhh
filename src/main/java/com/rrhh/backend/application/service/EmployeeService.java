package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.web.dto.employee.*;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO create(EmployeeRequestDTO dto);
    EmployeeResponseDTO update(Long id, EmployeeUpdateDTO dto);
    EmployeeResponseDTO updateStatusToRetired(Long id);
    List<EmployeeResponseDTO> searchByName(String name);
    EmployeeResponseDTO getById(Long id);
    List<EmployeeResponseDTO> getByDepartment(Long departmentId);
    List<EmployeeResponseDTO> getByPosition(Long positionId);
    List<EmployeeResponseDTO> getByHireDateRange(LocalDate start, LocalDate end);
    List<EmployeeResponseDTO> getAllEmployees();
    List<EmployeeResponseDTO> filterByStatus(EmployeeStatus status);
    EmployeeDashboardDTO getEmployeeDashboard(String username);
    EmployeeStatsDTO getEmployeeStats(String username);
}
