package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.email.EmailService;
import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.LeaveRequestMapper;
import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.application.utils.FileStorageService;
import com.rrhh.backend.application.validator.LeaveRequestValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.leave.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveRequestValidator validator;
    private final EmailService emailService;

    @Override
    @Transactional
    public LeaveRequestCreateDTO createLeaveRequest(
            LeaveRequestCreateDTO dto, Long employeeId, MultipartFile evidenceFile) {

        validator.validateCreate(dto, employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        String evidenceImagePath = null;
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            evidenceImagePath = fileStorageService.storeFile(evidenceFile, employeeId);
        }

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(dto, employee, evidenceImagePath);
        leaveRequestRepository.save(leaveRequest);

        log.info("Solicitud creada: empleadoId={}, tipo={}, fechas=[{} - {}]",
                employeeId, dto.getType(), dto.getStartDate(), dto.getEndDate());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestEmployeeDTO> getMyLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByRequestDateDesc(employeeId)
                .stream().map(leaveRequestMapper::toEmployeeDTO).collect(Collectors.toList());
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

        int years = calculateYearsOfService(employee.getHireDate());
        int max   = years < 1 ? 0 : years < 2 ? 15 : 30;

        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear   = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        int used = (int) leaveRequestRepository
                .findApprovedByEmployeeTypeAndDateRange(employeeId, LeaveType.VACACIONES, startOfYear, endOfYear)
                .stream().mapToLong(lr -> countWorkingDays(lr.getStartDate(), lr.getEndDate())).sum();

        return LeaveBalanceDTO.builder()
                .employeeId(employeeId).employeeName(employee.getFullName())
                .yearsOfService(years).maxVacationDays(max)
                .usedVacationDays(used).availableVacationDays(max - used)
                .build();
    }

    // ── Jefe ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHeadDTO> getPendingRequestsForHead(Long headEmployeeId) {
        Employee head = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        return leaveRequestRepository.findPendingByDepartment(head.getDepartment().getId())
                .stream().map(leaveRequestMapper::toHeadDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHeadDTO> getAllRequestsForHead(Long headEmployeeId) {
        Employee head = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        return leaveRequestRepository.findByDepartment(head.getDepartment().getId())
                .stream().map(leaveRequestMapper::toHeadDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestHeadDTO getRequestDetailForHead(Long requestId, Long headEmployeeId) {
        return leaveRequestMapper.toHeadDTO(
                leaveRequestRepository.findByIdAndDepartmentHead(requestId, headEmployeeId)
                        .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada o sin permisos")));
    }

    @Override
    @Transactional
    public void respondAsHead(LeaveHeadResponseDTO responseDTO, Long headEmployeeId) {
        Employee headEmployee = employeeRepository.findById(headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        LeaveRequest request = leaveRequestRepository
                .findByIdAndDepartmentHead(responseDTO.getRequestId(), headEmployeeId)
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada o sin permisos"));

        if (request.getStatus() != LeaveStatus.PENDIENTE_JEFE) {
            throw new ErrorSistema("La solicitud ya fue procesada");
        }

        leaveRequestMapper.applyHeadResponse(request, responseDTO, headEmployee);

        if (responseDTO.getStatus() == LeaveStatus.APROBADO) {
            request.setStatus(LeaveStatus.PENDIENTE_RRHH);
        }

        leaveRequestRepository.save(request);

        // ← Notificación async — no bloquea el response HTTP
        emailService.sendHeadResponseNotification(request);

        log.info("Jefe respondió solicitud id={}: estado={}", request.getId(), request.getStatus());
    }

    // ── RRHH ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHRDTO> getPendingRequestsForHR() {
        return leaveRequestRepository.findPendingForHR()
                .stream().map(leaveRequestMapper::toHRDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestHRDTO> getAllRequestsForHR() {
        return leaveRequestRepository.findAllOrderByRequestDateDesc()
                .stream().map(leaveRequestMapper::toHRDTO).collect(Collectors.toList());
    }

    // Versión paginada para el controller
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LeaveRequestHRDTO> getAllRequestsForHR(Pageable pageable) {
        return PagedResponse.of(
                leaveRequestRepository.findAllOrderByRequestDateDesc(pageable)
                        .map(leaveRequestMapper::toHRDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestHRDTO getRequestDetailForHR(Long requestId) {
        return leaveRequestMapper.toHRDTO(
                leaveRequestRepository.findByIdWithDetails(requestId)
                        .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada")));
    }

    @Override
    @Transactional
    public void respondAsHR(LeaveHRResponseDTO responseDTO) {
        LeaveRequest request = leaveRequestRepository.findById(responseDTO.getRequestId())
                .orElseThrow(() -> new ErrorSistema("Solicitud no encontrada"));

        if (request.getStatus() != LeaveStatus.PENDIENTE_RRHH) {
            throw new ErrorSistema("La solicitud no está pendiente de RRHH");
        }

        leaveRequestMapper.applyHRResponse(request, responseDTO, null);
        leaveRequestRepository.save(request);

        // ← Notificación async — email de resolución final al empleado
        emailService.sendHRResponseNotification(request);

        log.info("RRHH respondió solicitud id={}: estado={}", request.getId(), request.getStatus());
    }

    // ── Auxiliares ────────────────────────────────────────────────────────

    private int calculateYearsOfService(LocalDate hireDate) {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    private long countWorkingDays(LocalDate start, LocalDate end) {
        long days = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (!EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek())) days++;
            date = date.plusDays(1);
        }
        return days;
    }
}
