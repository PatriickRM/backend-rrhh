package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Position;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByTitleAndDepartment_Id(String title, Long departmentId);
    List<Position> findByTitleContainingIgnoreCase(String title);
    @EntityGraph(attributePaths = "department") // -> Entitygraph para incluir el nombre del departamento con sus posiciones
    List<Position> findByDepartmentId(Long departmentId);
}
