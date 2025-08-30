package com.rrhh.backend.application.mapper.impl;

import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.web.dto.employee.*;
import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public EmployeeSummaryDTO toSummary(Employee employee) {
        return EmployeeSummaryDTO.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .positionName(employee.getPosition().getTitle())
                .build();
    }

    @Override
    public EmployeeResponseDTO toDto(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .dni(employee.getDni())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .dateOfBirth(employee.getDateOfBirth())
                .hireDate(employee.getHireDate())
                .contractEndDate(employee.getContractEndDate())
                .positionTitle(employee.getPosition().getTitle())
                .departmentName(employee.getDepartment().getName())
                .salary(employee.getSalary())
                .status(employee.getStatus())
                .gender(employee.getGender())
                .build();
    }

    @Override
    public Employee toEntity(EmployeeRequestDTO dto, User user, Position position, Department department) {
        return Employee.builder()
                .user(user)
                .fullName(dto.getFullName())
                .dni(dto.getDni())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .hireDate(dto.getHireDate())
                .contractEndDate(dto.getContractEndDate())
                .position(position)
                .department(department)
                .salary(dto.getSalary())
                .status(EmployeeStatus.CONTRATADO)
                .gender(dto.getGender())
                .build();
    }

    @Override
    public void updateEntity(Employee entity, EmployeeUpdateDTO dto,Department department, Position position) {
        entity.setFullName(dto.getFullName());
        entity.setDni(dto.getDni());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setHireDate(dto.getHireDate());
        entity.setContractEndDate(dto.getContractEndDate());
        entity.setSalary(dto.getSalary());
        entity.setStatus(dto.getStatus());
        entity.setGender(dto.getGender());
        entity.setDepartment(department);
        entity.setPosition(position);
    }
    @Override
    public EmployeeDashboardDTO toDashboardDto(Employee employee, LeaveBalanceDTO leaveBalance, List<LeaveRequest> requests) {
        return EmployeeDashboardDTO.builder()
                .employee(toDto(employee))
                .leaveBalance(leaveBalance)
                .stats(toStatsDto(requests))
                .build();
    }

    @Override
    public EmployeeStatsDTO toStatsDto(List<LeaveRequest> requests) {
        // Filtrar por año actual
        int currentYear = LocalDate.now().getYear();
        List<LeaveRequest> currentYearRequests = requests.stream()
                .filter(req -> req.getStartDate() != null && req.getStartDate().getYear() == currentYear)
                .collect(Collectors.toList());

        return EmployeeStatsDTO.builder()
                .totalRequests(currentYearRequests.size())
                .approvedRequests(countByStatus(currentYearRequests, LeaveStatus.APROBADO))
                .pendingRequests(countPending(currentYearRequests))
                .rejectedRequests(countByStatus(currentYearRequests, LeaveStatus.RECHAZADO))
                .requestsByMonth(calculateMonthlyStats(currentYearRequests))
                .requestsByType(calculateTypeStats(currentYearRequests))
                .averageResponseDays(calculateAverageResponseTime(currentYearRequests))
                .build();
    }
    private int countByStatus(List<LeaveRequest> requests, LeaveStatus status) {
        return (int) requests.stream()
                .filter(req -> req.getStatus() == status)
                .count();
    }

    private int countPending(List<LeaveRequest> requests) {
        return (int) requests.stream()
                .filter(req -> req.getStatus() == LeaveStatus.PENDIENTE_JEFE ||
                        req.getStatus() == LeaveStatus.PENDIENTE_RRHH)
                .count();
    }

    private List<MonthlyStatsDTO> calculateMonthlyStats(List<LeaveRequest> requests) {
        Map<String, Long> monthlyCount = requests.stream()
                .collect(Collectors.groupingBy(
                        req -> req.getRequestDate().getMonth().getDisplayName(
                                TextStyle.SHORT, Locale.getDefault()
                        ),
                        Collectors.counting()
                ));

        return monthlyCount.entrySet().stream()
                .map(entry -> MonthlyStatsDTO.builder()
                        .month(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TypeStatsDTO> calculateTypeStats(List<LeaveRequest> requests) {
        int totalRequests = requests.size();
        if (totalRequests == 0) return new ArrayList<>();

        Map<LeaveType, Long> typeCount = requests.stream()
                .collect(Collectors.groupingBy(
                        LeaveRequest::getType,
                        Collectors.counting()
                ));

        return typeCount.entrySet().stream()
                .map(entry -> TypeStatsDTO.builder()
                        .type(getLeaveTypeDisplayName(entry.getKey()))
                        .count(entry.getValue().intValue())
                        .percentage(Math.round((entry.getValue() * 100.0 / totalRequests) * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());
    }

    private double calculateAverageResponseTime(List<LeaveRequest> requests) {
        List<LeaveRequest> respondedRequests = requests.stream()
                .filter(req -> req.getHeadResponseDate() != null)
                .collect(Collectors.toList());

        if (respondedRequests.isEmpty()) return 0.0;

        double totalDays = respondedRequests.stream()
                .mapToLong(req -> ChronoUnit.DAYS.between(
                        req.getRequestDate().toLocalDate(),
                        req.getHeadResponseDate().toLocalDate()
                ))
                .average()
                .orElse(0.0);

        return Math.round(totalDays * 100.0) / 100.0;
    }

    private String getLeaveTypeDisplayName(LeaveType type) {
        switch (type) {
            case VACACIONES: return "Vacaciones";
            case ENFERMEDAD: return "Enfermedad";
            case MATRIMONIO: return "Matrimonio";
            case FALLECIMIENTO_FAMILIAR: return "Fallecimiento Familiar";
            case NACIMIENTO_HIJO: return "Nacimiento de Hijo";
            case MUDANZA: return "Mudanza";
            case CITA_MEDICA: return "Cita Médica";
            default: return type.name();
        }
    }

}
