package com.rrhh.backend.application.service;


import com.rrhh.backend.web.dto.position.PositionRequestDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;
import com.rrhh.backend.web.dto.position.PositionStatusUpdateDTO;
import com.rrhh.backend.web.dto.position.PositionUpdateDTO;

import java.util.List;

public interface PositionService {
    PositionResponseDTO create(PositionRequestDTO dto);
    List<PositionResponseDTO> getAllPositions();
    PositionResponseDTO getPositionById(Long id);
    PositionResponseDTO updatePosition(Long id, PositionUpdateDTO dto);
    PositionResponseDTO updatePositionStatus(Long id, PositionStatusUpdateDTO dto);
    List<PositionResponseDTO> getPositionsByName(String titleFilter);
    List<PositionResponseDTO> getPositionsByDepartment(Long departmentId);
    List<PositionResponseDTO> getDisabledPositions();

}
