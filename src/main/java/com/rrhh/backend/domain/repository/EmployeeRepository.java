package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByDni(String dni);
    boolean existsByEmail(String email);
    List<Employee> findByFullNameContainingIgnoreCase(String name);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByPositionId(Long positionId);
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByHireDateBetween(LocalDate start, LocalDate end);
}
