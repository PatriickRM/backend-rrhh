package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.LeaveRequestMapper;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.domain.model.LeaveStatus;
import com.rrhh.backend.web.dto.leave.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class LeaveRequestMapperImpl implements LeaveRequestMapper {

    @Override
    public LeaveRequest toEntity(LeaveRequestCreateDTO dto, Employee employee, String evidenceImagePath) {
        return LeaveRequest.builder()
                .employee(employee)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(dto.getType())
                .status(LeaveStatus.PENDIENTE_JEFE)
                .justification(dto.getJustification())
                .evidenceImagePath(evidenceImagePath)
                .requestDate(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    public LeaveRequestEmployeeDTO toEmployeeDTO(LeaveRequest entity) {
        return LeaveRequestEmployeeDTO.builder()
                .id(entity.getId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .type(entity.getType())
                .status(entity.getStatus())
                .justification(entity.getJustification())
                .headComment(entity.getHeadComment())
                .hrComment(entity.getHrComment())
                .requestDate(entity.getRequestDate())
                .headResponseDate(entity.getHeadResponseDate())
                .hrResponseDate(entity.getHrResponseDate())
                .build();
    }


    @Override
    public LeaveRequestHeadDTO toHeadDTO(LeaveRequest entity) {
        return LeaveRequestHeadDTO.builder()
                .id(entity.getId())
                .employeeName(entity.getEmployee().getFullName())
                .employeeDepartment(entity.getEmployee().getDepartment().getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .type(entity.getType())
                .status(entity.getStatus())
                .justification(entity.getJustification())
                .requestDate(entity.getRequestDate())
                .headResponseDate(entity.getHeadResponseDate())
                .build();
    }

    @Override
    public LeaveRequestHRDTO toHRDTO(LeaveRequest entity) {
        return LeaveRequestHRDTO.builder()
                .id(entity.getId())
                .employeeName(entity.getEmployee().getFullName())
                .employeeDepartment(entity.getEmployee().getDepartment().getName())
                .type(entity.getType())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .justification(entity.getJustification())
                .headComment(entity.getHeadComment())
                .hrComment(entity.getHrComment())
                .reviewedByHeadName(entity.getReviewedByHeadName())
                .requestDate(entity.getRequestDate())
                .headResponseDate(entity.getHeadResponseDate())
                .hrResponseDate(entity.getHrResponseDate())
                .build();
    }


    @Override
    public void applyHeadResponse(LeaveRequest entity, LeaveHeadResponseDTO dto, Employee head) {
        entity.setStatus(dto.getStatus());
        entity.setHeadComment(dto.getComment());
        entity.setReviewedByHeadName(head.getFullName());
        entity.setHeadResponseDate(LocalDateTime.now());
    }

    @Override
    public void applyHRResponse(LeaveRequest entity, LeaveHRResponseDTO dto, Employee hr) {
        entity.setStatus(dto.getStatus());
        entity.setHrComment(dto.getComment());
        entity.setHrResponseDate(LocalDateTime.now());
    }

    @Override
    public List<LeaveRequestEmployeeDTO> toEmployeeDTOs(List<LeaveRequest> entities) {
        return entities.stream()
                .map(this::toEmployeeDTO)
                .toList();
    }

    @Override
    public List<LeaveRequestHeadDTO> toHeadDTOs(List<LeaveRequest> entities) {
        return entities.stream()
                .map(this::toHeadDTO)
                .toList();
    }

    @Override
    public List<LeaveRequestHRDTO> toHRDTOs(List<LeaveRequest> entities) {
        return entities.stream()
                .map(this::toHRDTO)
                .toList();
    }
}
