package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.web.dto.employee.EmployeeSummaryDTO;

public interface EmployeeMapper {
    EmployeeSummaryDTO toSummary (Employee employee);
}
