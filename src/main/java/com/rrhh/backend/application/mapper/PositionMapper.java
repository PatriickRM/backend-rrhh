package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.web.dto.position.PositionRequestDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;

import com.rrhh.backend.web.dto.position.PositionUpdateDTO;

public interface PositionMapper {
    Position toEntity(PositionRequestDTO dto);
    PositionResponseDTO toDto(Position entity);
    void updateEntity(PositionUpdateDTO updateDTO, Position entity );
}
