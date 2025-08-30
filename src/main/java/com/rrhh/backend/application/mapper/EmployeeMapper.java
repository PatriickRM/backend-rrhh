package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.web.dto.employee.*;
import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;

import java.util.List;

public interface EmployeeMapper {
    EmployeeSummaryDTO toSummary (Employee employee);
    EmployeeResponseDTO toDto(Employee employee);
    Employee toEntity(EmployeeRequestDTO dto, User user, Position position, Department department);
    void updateEntity(Employee entity, EmployeeUpdateDTO dto,Department department, Position position);
    EmployeeDashboardDTO toDashboardDto(Employee employee, LeaveBalanceDTO leaveBalance, List<LeaveRequest> requests);
    EmployeeStatsDTO toStatsDto(List<LeaveRequest> requests);
}
