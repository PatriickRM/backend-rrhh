package com.rrhh.backend.application.mapper.impl;


import com.rrhh.backend.application.mapper.DepartmentMapper;
import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.application.mapper.PositionMapper;
import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.web.dto.department.DepartmentRequestDTO;
import com.rrhh.backend.web.dto.department.DepartmentResponseDTO;
import com.rrhh.backend.web.dto.department.DepartmentUpdateDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class DepartmentMapperImpl implements DepartmentMapper {
    private final PositionMapper positionMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    public Department toEntity(DepartmentRequestDTO dto) {
        return Department.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    @Override
    public DepartmentResponseDTO toDto(Department entity) {
        return DepartmentResponseDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .head(Optional.ofNullable(entity.getHead()).map(employeeMapper::toSummary).orElse(null)) //Jefe de departamento es opcional no siempre se inicia con uno
                .positions(entity.getPositions() != null ? entity.getPositions().stream().map(positionMapper::toDto)
                        .collect(Collectors.toList()) : Collections.emptyList()) // devuelve lista vacia en el dto para evitar errores si no hay posiciones
                .build();
    }

    @Override
    public void updateEntity(DepartmentUpdateDTO dto, Department entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }
}
