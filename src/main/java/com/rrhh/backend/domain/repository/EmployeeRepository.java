package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
