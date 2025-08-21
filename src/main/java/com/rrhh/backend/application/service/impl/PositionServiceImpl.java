package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.PositionMapper;
import com.rrhh.backend.application.service.PositionService;
import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.domain.repository.DepartmentRepository;
import com.rrhh.backend.domain.repository.PositionRepository;
import com.rrhh.backend.web.dto.position.PositionRequestDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;
import com.rrhh.backend.web.dto.position.PositionStatusUpdateDTO;
import com.rrhh.backend.web.dto.position.PositionUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional
    public PositionResponseDTO create(PositionRequestDTO dto) {
        Department dep = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(()-> new ErrorSistema("Departamento no encotrado"));
        if(positionRepository.existsByTitleAndDepartment_Id(dto.getTitle(), dto.getDepartmentId())){
            throw new ErrorSistema("Ya existe una posicion con ese Titulo en este departamento!");
        }
        if(dto.getBaseSalary() != null && dto.getBaseSalary().compareTo(BigDecimal.valueOf(1200)) < 0){
            throw new ErrorSistema("El salario debe ser mayor a 1200 debido a las leyes!");
        }
        Position position = positionMapper.toEntity(dto);
        position.setDepartment(dep);

        return positionMapper.toDto(positionRepository.save(position));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getAllPositions() {
        return positionRepository.findAll()
                .stream()
                .map(positionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponseDTO getPositionById(Long id) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new ErrorSistema("Posicion con dicha id no existe"));
        return positionMapper.toDto(position);
    }

    @Override
    @Transactional
    public PositionResponseDTO updatePosition(Long id, PositionUpdateDTO dto) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new ErrorSistema("Posicion con dicha id no encontrada"));

        boolean titleChange = !position.getTitle().equals(dto.getTitle());
        boolean depChange = !position.getDepartment().getId().equals(dto.getDepartmentId());
        if((titleChange || depChange)  && positionRepository.existsByTitleAndDepartment_Id(dto.getTitle(), dto.getDepartmentId())){
            throw new ErrorSistema("Ya existe esa posición en este departamento!");
        }
        if (dto.getBaseSalary() != null && dto.getBaseSalary().compareTo(BigDecimal.valueOf(1200)) < 0) {
            throw new ErrorSistema("El salario debe ser mayor a 1200 debido a las leyes!");
        }
        positionMapper.updateEntity(dto,position);

        if (depChange) {
            Department dep = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ErrorSistema("Departamento no encontrado"));
            position.setDepartment(dep);
        }
        return positionMapper.toDto(positionRepository.save(position));
    }

    @Override
    @Transactional
    public PositionResponseDTO updatePositionStatus(Long id, PositionStatusUpdateDTO dto) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new ErrorSistema("Posicion con dicha id no encontrada"));
        position.setEnabled(dto.getEnabled());
        return positionMapper.toDto(positionRepository.save(position));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getPositionsByName(String titleFilter) {
        if (titleFilter == null || titleFilter.isBlank()) {
            return positionRepository.findAll()
                    .stream()
                    .map(positionMapper::toDto)
                    .toList(); //Devolver todos si no se pone nada
        }
        return positionRepository.findByTitleContainingIgnoreCase(titleFilter)
                .stream()
                .map(positionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getPositionsByDepartment(Long departmentId) {
        return positionRepository.findByDepartmentId(departmentId).stream()
                .map(positionMapper::toDto)
                .toList();
    }

}
