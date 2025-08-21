package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByCode(String code);
    boolean existsByName(String name);
    List<Department> findByNameContainingIgnoreCase(String name);
}
