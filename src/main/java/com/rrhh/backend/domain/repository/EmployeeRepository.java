package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByDni(String dni);
    boolean existsByEmail(String email);
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
