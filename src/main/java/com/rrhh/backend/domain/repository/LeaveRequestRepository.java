package com.rrhh.backend.domain.repository;

import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.domain.model.LeaveStatus;
import com.rrhh.backend.domain.model.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {


    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.status IN :statuses " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    boolean existsByEmployeeIdAndStatusInAndDateRangeOverlap(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.status IN :statuses " +
            "ORDER BY lr.startDate ASC")
    List<LeaveRequest> findByEmployeeIdAndStatusIn(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<LeaveStatus> statuses
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.status IN ('APROBADO', 'PENDIENTE_JEFE', 'PENDIENTE_RRHH') " +
            "AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findApprovedByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.type = :type " +
            "AND lr.status IN ('APROBADO', 'PENDIENTE_JEFE', 'PENDIENTE_RRHH') " +
            "AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findApprovedByEmployeeTypeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("type") LeaveType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.type = :type " +
            "AND lr.status IN ('APROBADO', 'PENDIENTE_JEFE', 'PENDIENTE_RRHH')")
    boolean existsApprovedByEmployeeAndType(
            @Param("employeeId") Long employeeId,
            @Param("type") LeaveType type
    );

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "ORDER BY lr.requestDate DESC")
    List<LeaveRequest> findByEmployeeIdOrderByRequestDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.department.id = :departmentId " +
            "AND lr.status = 'PENDIENTE_JEFE' " +
            "ORDER BY lr.requestDate ASC")
    List<LeaveRequest> findPendingByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.department.id = :departmentId " +
            "ORDER BY lr.requestDate DESC")
    List<LeaveRequest> findByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.status = 'PENDIENTE_RRHH' " +
            "ORDER BY lr.requestDate ASC")
    List<LeaveRequest> findPendingForHR();

    @Query("SELECT lr FROM LeaveRequest lr ORDER BY lr.requestDate DESC")
    List<LeaveRequest> findAllOrderByRequestDateDesc();

    // Métodos para próximos permisos
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.status = 'APROBADO' " +
            "AND lr.startDate >= CURRENT_DATE " +
            "AND lr.startDate <= :futureDate " +
            "ORDER BY lr.startDate ASC")
    List<LeaveRequest> findUpcomingApprovedLeaves(@Param("futureDate") LocalDate futureDate);

    // Método para buscar por ID con toda la información
    @Query("SELECT lr FROM LeaveRequest lr " +
            "LEFT JOIN FETCH lr.employee e " +
            "LEFT JOIN FETCH e.department d " +
            "LEFT JOIN FETCH e.position p " +
            "WHERE lr.id = :id")
    Optional<LeaveRequest> findByIdWithDetails(@Param("id") Long id);

    // Métodos para validación de autoridad del jefe
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.id = :requestId " +
            "AND lr.employee.department.head.id = :headId")
    Optional<LeaveRequest> findByIdAndDepartmentHead(
            @Param("requestId") Long requestId,
            @Param("headId") Long headId
    );

    /**
     * Cuenta el número de sesiones de vacaciones en un año específico
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.type = 'VACACIONES' " +
            "AND lr.status IN ('APROBADO', 'PENDIENTE_JEFE', 'PENDIENTE_RRHH') " +
            "AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    Long countVacationSessionsByEmployeeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}