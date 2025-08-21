package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.DepartmentMapper;
import com.rrhh.backend.application.service.DepartmentService;
import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.domain.repository.DepartmentRepository;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.web.dto.department.DepartmentRequestDTO;
import com.rrhh.backend.web.dto.department.DepartmentResponseDTO;
import com.rrhh.backend.web.dto.department.DepartmentStatusUpdateDTO;
import com.rrhh.backend.web.dto.department.DepartmentUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public DepartmentResponseDTO create(DepartmentRequestDTO dto) {
        if(departmentRepository.existsByCode(dto.getCode())){
            throw new ErrorSistema("Codigo de Departamento ya existe!");
        }
        if(departmentRepository.existsByName(dto.getName())){
            throw new ErrorSistema("Nombre de departamento ya existente!");
        }
        Department department = departmentMapper.toEntity(dto);
        department.setEnabled(true);

        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new ErrorSistema("Departamento no encontrado."));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentUpdateDTO dto) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new ErrorSistema("Departamento no encontrado."));

        if(!department.getCode().equals(dto.getCode()) && departmentRepository.existsByCode(dto.getCode())){
            throw new ErrorSistema("Ya existe un departamento con ese codigo");
        }
        if(!department.getName().equals(dto.getName()) && departmentRepository.existsByName(dto.getName())){
            throw new ErrorSistema("Ya existe un departamento con ese nombre");
        }
        departmentMapper.updateEntity(dto,department);
        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponseDTO updateStatus(Long id, DepartmentStatusUpdateDTO dto) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new ErrorSistema("Departamento no encontrado."));
        department.setEnabled(dto.getEnabled());
        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> findByName(String nameFilter) {
        if(nameFilter == null || nameFilter.isBlank()){
            return departmentRepository.findAll()
                    .stream()
                    .map(departmentMapper::toDto)
                    .toList();
        }
        return departmentRepository.findByNameContainingIgnoreCase(nameFilter)
                .stream()
                .map(departmentMapper::toDto)
                .toList();
    }
}
