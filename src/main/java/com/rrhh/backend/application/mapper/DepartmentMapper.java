package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.web.dto.department.DepartmentRequestDTO;
import com.rrhh.backend.web.dto.department.DepartmentResponseDTO;
import com.rrhh.backend.web.dto.department.DepartmentUpdateDTO;

public interface DepartmentMapper {
    Department toEntity(DepartmentRequestDTO dto);
    DepartmentResponseDTO toDto(Department entity);
    void updateEntity(DepartmentUpdateDTO dto, Department entity);
}
