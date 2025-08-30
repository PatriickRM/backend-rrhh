package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.DepartmentMapper;
import com.rrhh.backend.application.service.DepartmentService;
import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.repository.DepartmentRepository;
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
        return departmentRepository.findAllWithHeadAndPositions()
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

    @Override
    public void updateHeadIfChanged(Employee employee) {
        //Evitar cambios innecesarios
        if (!employee.getPosition().getTitle().equalsIgnoreCase("Jefe de Departamento")) {
            return;
        }


        Department department = employee.getDepartment();
        Employee currentHead = department.getHead();

        // Evitar cambios innecesarios
        if (currentHead != null && currentHead.getId().equals(employee.getId())) {
            return;
        }

        // Asignar nuevo jefe si se actualiza al jefe al agregar empleado
        department.setHead(employee);
        departmentRepository.save(department);
    }

    @Override
    public void removeHeadIfChanged(Employee employee) {
        if (employee.getPosition().getTitle().equalsIgnoreCase("Jefe de Departamento")) {
            return; // Si sigue siendo jefe, no remover
        }
        Department department = employee.getDepartment();
        Employee currentHead = department.getHead();

        if (currentHead != null && currentHead.getId().equals(employee.getId())) {
            department.setHead(null);
            departmentRepository.save(department);
        }
    }
}
