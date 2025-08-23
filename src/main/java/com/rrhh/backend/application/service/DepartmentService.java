package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.web.dto.department.DepartmentRequestDTO;
import com.rrhh.backend.web.dto.department.DepartmentResponseDTO;
import com.rrhh.backend.web.dto.department.DepartmentStatusUpdateDTO;
import com.rrhh.backend.web.dto.department.DepartmentUpdateDTO;

import java.util.List;

public interface DepartmentService {
    DepartmentResponseDTO create(DepartmentRequestDTO dto);
    List<DepartmentResponseDTO> getAllDepartments();
    DepartmentResponseDTO getDepartmentById(Long id);
    DepartmentResponseDTO updateDepartment(Long id, DepartmentUpdateDTO dto);
    DepartmentResponseDTO updateStatus(Long id, DepartmentStatusUpdateDTO dto);
    List<DepartmentResponseDTO> findByName(String nameFilter);
    void updateHeadIfChanged(Employee employee);
    void removeHeadIfChanged(Employee employee);
}
