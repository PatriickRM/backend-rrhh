package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.domain.model.User;
import com.rrhh.backend.web.dto.employee.EmployeeRequestDTO;
import com.rrhh.backend.web.dto.employee.EmployeeResponseDTO;
import com.rrhh.backend.web.dto.employee.EmployeeSummaryDTO;
import com.rrhh.backend.web.dto.employee.EmployeeUpdateDTO;

public interface EmployeeMapper {
    EmployeeSummaryDTO toSummary (Employee employee);
    EmployeeResponseDTO toDto(Employee employee);
    Employee toEntity(EmployeeRequestDTO dto, User user, Position position, Department department);
    void updateEntity(Employee entity, EmployeeUpdateDTO dto,Department department, Position position);
}
