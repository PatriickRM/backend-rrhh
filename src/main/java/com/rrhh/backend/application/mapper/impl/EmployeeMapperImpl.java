package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.web.dto.employee.EmployeeSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public EmployeeSummaryDTO toSummary(Employee employee) {
        return EmployeeSummaryDTO.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .positionName(employee.getPosition().getTitle())
                .build();
    }
}
