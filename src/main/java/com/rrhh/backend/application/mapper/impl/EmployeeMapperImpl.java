package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.web.dto.employee.EmployeeRequestDTO;
import com.rrhh.backend.web.dto.employee.EmployeeResponseDTO;
import com.rrhh.backend.web.dto.employee.EmployeeSummaryDTO;
import com.rrhh.backend.web.dto.employee.EmployeeUpdateDTO;
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

    @Override
    public EmployeeResponseDTO toDto(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .dni(employee.getDni())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .dateOfBirth(employee.getDateOfBirth())
                .hireDate(employee.getHireDate())
                .contractEndDate(employee.getContractEndDate())
                .positionTitle(employee.getPosition().getTitle())
                .departmentName(employee.getDepartment().getName())
                .salary(employee.getSalary())
                .status(employee.getStatus())
                .gender(employee.getGender())
                .build();
    }

    @Override
    public Employee toEntity(EmployeeRequestDTO dto, User user, Position position, Department department) {
        return Employee.builder()
                .user(user)
                .fullName(dto.getFullName())
                .dni(dto.getDni())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .hireDate(dto.getHireDate())
                .contractEndDate(dto.getContractEndDate())
                .position(position)
                .department(department)
                .salary(dto.getSalary())
                .status(EmployeeStatus.CONTRATADO)
                .gender(dto.getGender())
                .build();
    }

    @Override
    public void updateEntity(Employee entity, EmployeeUpdateDTO dto,Department department, Position position) {
        entity.setFullName(dto.getFullName());
        entity.setDni(dto.getDni());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setHireDate(dto.getHireDate());
        entity.setContractEndDate(dto.getContractEndDate());
        entity.setSalary(dto.getSalary());
        entity.setStatus(dto.getStatus());
        entity.setGender(dto.getGender());
        entity.setDepartment(department);
        entity.setPosition(position);
    }

}
