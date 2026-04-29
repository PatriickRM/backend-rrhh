package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByDni(String dni);
    boolean existsByEmail(String email);

    // ── Paginados (nuevos) ────────────────────────────────────────────────
    Page<Employee> findAll(Pageable pageable);
    Page<Employee> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findByPositionId(Long positionId, Pageable pageable);
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    Page<Employee> findByHireDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    // ── Sin paginar — mantenidos para uso interno (status checks, etc.) ───
    List<Employee> findByFullNameContainingIgnoreCase(String name);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByPositionId(Long positionId);
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByHireDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Employee e WHERE e.id IN (SELECT d.head.id FROM Department d WHERE d.head IS NOT NULL)")
    List<Employee> findDepartmentHeads();

    @Query("SELECT e FROM Employee e WHERE e.user.username = :username")
    Optional<Employee> findByUserUsername(@Param("username") String username);
}
