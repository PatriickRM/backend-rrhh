package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.PositionMapper;
import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.web.dto.position.PositionRequestDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;
import com.rrhh.backend.web.dto.position.PositionUpdateDTO;
import org.springframework.stereotype.Component;

@Component
public class PositionMapperImpl implements PositionMapper {

    @Override
    public Position toEntity(PositionRequestDTO dto) {
        return Position.builder()
                .title(dto.getTitle())
                .baseSalary(dto.getBaseSalary())
                .enabled(true)
                .build();
    }

    @Override
    public PositionResponseDTO toDto(Position entity) {
        return PositionResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .baseSalary(entity.getBaseSalary())
                .enabled(entity.getEnabled())
                .build();
    }

    @Override
    public void updateEntity(PositionUpdateDTO updateDTO, Position entity) {
        entity.setTitle(updateDTO.getTitle());
        entity.setBaseSalary(updateDTO.getBaseSalary());
    }
}
