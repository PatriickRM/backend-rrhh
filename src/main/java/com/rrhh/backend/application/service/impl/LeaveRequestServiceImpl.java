package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.LeaveRequestMapper;
import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.application.utils.FileStorageService;
import com.rrhh.backend.application.validator.LeaveRequestValidator;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.domain.model.LeaveStatus;
import com.rrhh.backend.domain.model.LeaveType;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import com.rrhh.backend.web.dto.leave.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveRequestValidator validator;

    @Override
    @Transactional
    public LeaveRequestCreateDTO createLeaveRequest(LeaveRequestCreateDTO dto, Long employeeId, MultipartFile evidenceFile) {
        // Usar las validaciones
        validator.validateCreate(dto, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        // Guardar evidencia si existe
        String evidenceImagePath = null;
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            evidenceImagePath = fileStorageService.storeFile(evidenceFile, employeeId);
        }

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(dto, employee, evidenceImagePath);
        leaveRequestRepository.save(leaveRequest);

        return dto; // Retorna el DTO que se usó para crear
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestEmployeeDTO> getMyLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByRequestDateDesc(employeeId)
                .stream()
                .map(leaveRequestMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestEmployeeDTO getMyLeaveRequestDetail(Long requestId, Long employeeId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada"));

        if (!request.getEmployee().getId().equals(employeeId)) {
            throw new ErrorSistema("No tiene permisos para ver esta solicitud");
        }

        return leaveRequestMapper.toEmployeeDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceDTO getEmployeeLeaveBalance(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        int yearsOfService = calculateYearsOfService(employee.getHireDate());
        int maxVacationDays;
        if (yearsOfService < 1) {
            maxVacationDays = 0;
        } else if (yearsOfService < 2) {
            maxVacationDays = 15;
        } else {
            maxVacationDays = 30;
        }
        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        List<LeaveRequest> approvedVacationsThisYear = leaveRequestRepository
                .findApprovedByEmployeeTypeAndDateRange(employeeId, LeaveType.VACACIONES, startOfYear, endOfYear);

        int usedVacationDays = (int) approvedVacationsThisYear.stream()
                .mapToLong(lr -> countWorkingDays(lr.getStartDate(), lr.getEndDate()))
                .sum();

        return LeaveBalanceDTO.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .yearsOfService(yearsOfService)
                .maxVacationDays(maxVacationDays)
                .usedVacationDays(usedVacationDays)
                .availableVacationDays(maxVacationDays - usedVacationDays)
                .build();
    }

    // ========================= MÉTODOS PARA JEFE DE DEPARTAMENTO =========================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHeadDTO> getPendingRequestsForHead(Long headEmployeeId) {
        Employee headEmployee = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        return leaveRequestRepository.findPendingByDepartment(headEmployee.getDepartment().getId())
                .stream()
                .map(leaveRequestMapper::toHeadDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHeadDTO> getAllRequestsForHead(Long headEmployeeId) {
        Employee headEmployee = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        return leaveRequestRepository.findByDepartment(headEmployee.getDepartment().getId())
                .stream()
                .map(leaveRequestMapper::toHeadDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestHeadDTO getRequestDetailForHead(Long requestId, Long headEmployeeId) {
        LeaveRequest request = leaveRequestRepository.findByIdAndDepartmentHead(requestId, headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada o sin permisos"));

        return leaveRequestMapper.toHeadDTO(request);
    }

    @Override
    @Transactional
    public void respondAsHead(LeaveHeadResponseDTO responseDTO, Long headEmployeeId) {
        Employee headEmployee = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        LeaveRequest request = leaveRequestRepository.findByIdAndDepartmentHead(responseDTO.getRequestId(), headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada o sin permisos"));

        if (request.getStatus() != LeaveStatus.PENDIENTE_JEFE) {
            throw new ErrorSistema("La solicitud ya fue procesada");
        }

        // Usar el mapper para aplicar la respuesta
        leaveRequestMapper.applyHeadResponse(request, responseDTO, headEmployee);

        // Si aprueba, cambiar estado a PENDIENTE_RRHH
        if (responseDTO.getStatus() == LeaveStatus.APROBADO) {
            request.setStatus(LeaveStatus.PENDIENTE_RRHH);
        }

        leaveRequestRepository.save(request);
    }

    // ========================= MÉTODOS PARA RECURSOS HUMANOS =========================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHRDTO> getPendingRequestsForHR() {
        return leaveRequestRepository.findPendingForHR()
                .stream()
                .map(leaveRequestMapper::toHRDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHRDTO> getAllRequestsForHR() {
        return leaveRequestRepository.findAllOrderByRequestDateDesc()
                .stream()
                .map(leaveRequestMapper::toHRDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestHRDTO getRequestDetailForHR(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada"));

        return leaveRequestMapper.toHRDTO(request);
    }

    @Override
    @Transactional
    public void respondAsHR(LeaveHRResponseDTO responseDTO) {
        LeaveRequest request = leaveRequestRepository.findById(responseDTO.getRequestId())
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada"));

        if (request.getStatus() != LeaveStatus.PENDIENTE_RRHH) {
            throw new ErrorSistema("La solicitud no está pendiente de RRHH");
        }

        request.setStatus(responseDTO.getStatus());
        request.setHrComment(responseDTO.getComment());
        request.setHrResponseDate(LocalDateTime.now());

        leaveRequestRepository.save(request);
    }

    //Validaciones
    private int calculateYearsOfService(LocalDate hireDate) {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    private long countWorkingDays(LocalDate start, LocalDate end) {
        long days = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (!EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek())) {
                days++;
            }
            date = date.plusDays(1);
        }
        return days;
    }
}
