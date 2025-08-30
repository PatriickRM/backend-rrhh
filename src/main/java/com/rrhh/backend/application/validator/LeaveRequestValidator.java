package com.rrhh.backend.application.validator;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import com.rrhh.backend.web.dto.leave.LeaveRequestCreateDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;

@Component
@AllArgsConstructor
public class LeaveRequestValidator {
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public void validateCreate(LeaveRequestCreateDTO dto, Long employeeId) {
        // 1. Validaciones básicas
        validateBasicRequirements(dto, employeeId);

        // 2. Validaciones específicas por tipo
        validateByLeaveType(dto, employeeId);

        // 3. Validaciones de fechas y días
        validateDateRules(dto, employeeId);

        // 4. Validaciones de solapamiento
        validateOverlap(dto, employeeId);

        // 5. Validaciones de reglas de negocio
        validateBusinessRules(dto, employeeId);
    }

    private void validateBasicRequirements(LeaveRequestCreateDTO dto, Long employeeId) {
        // Empleado válido y activo
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        if (employee.getStatus().equals(EmployeeStatus.RETIRADO)) {
            throw new ErrorSistema("No puede solicitar permisos un empleado retirado");
        }

        // Tipo de permiso no nulo
        if (dto.getType() == null) {
            throw new ErrorSistema("El tipo de permiso es obligatorio");
        }

        // Justificación obligatoria
        if (dto.getJustification() == null || dto.getJustification().trim().isEmpty()) {
            throw new ErrorSistema("La justificación es obligatoria");
        }

        if (dto.getJustification().length() < 10) {
            throw new ErrorSistema("La justificación debe tener al menos 10 caracteres");
        }
    }

    private void validateByLeaveType(LeaveRequestCreateDTO dto, Long employeeId) {
        var employee = employeeRepository.findById(employeeId).get();

        switch (dto.getType()) {
            case VACACIONES -> validateVacaciones(dto, employee);
            case ENFERMEDAD -> validateEnfermedad(dto, employee);
            case MATRIMONIO -> validateMatrimonio(dto, employee);
            case FALLECIMIENTO_FAMILIAR -> validateFallecimiento(dto, employee);
            case NACIMIENTO_HIJO -> validateNacimiento(dto, employee);
            case MUDANZA -> validateMudanza(dto, employee);
            case CITA_MEDICA -> validateCitaMedica(dto, employee);
        }
    }

    private void validateVacaciones(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        // Antigüedad mínima
        int aniosAntiguedad = calculateYearsOfService(employee.getHireDate());
        if (aniosAntiguedad < 1) {
            throw new ErrorSistema("Debe tener al menos 1 año de antigüedad para solicitar vacaciones");
        }

        // Días máximos según antigüedad
        int maxVacaciones = aniosAntiguedad >= 2 ? 30 : 15;

        // Validar bloques permitidos según antigüedad
        validateVacationBlocks(workingDaysRequested, maxVacaciones);

        // Anticipación mínima (15 días)
        if (dto.getStartDate().isBefore(LocalDate.now().plusDays(15))) {
            throw new ErrorSistema("Las vacaciones deben solicitarse con al menos 15 días de anticipación");
        }

        // Validar límite anual y sesiones
        validateVacationSessionsAndAnnualLimit(dto, employee.getId(), maxVacaciones, workingDaysRequested);
    }

    /**
     * Valida que los días solicitados correspondan a bloques permitidos
     */
    private void validateVacationBlocks(long workingDaysRequested, int maxVacaciones) {
        if (maxVacaciones == 30) {
            // Con 2+ años: solo bloques de 15 o 30 días
            if (workingDaysRequested != 15 && workingDaysRequested != 30) {
                throw new ErrorSistema("Las vacaciones solo pueden solicitarse en bloques de 15 o 30 días hábiles");
            }
        } else {
            // Con menos de 2 años: solo bloques de 15 días
            if (workingDaysRequested != 15) {
                throw new ErrorSistema("Con su antigüedad solo puede solicitar exactamente 15 días hábiles de vacaciones");
            }
        }
    }

    /**
     * Valida sesiones y límite anual de vacaciones
     */
    private void validateVacationSessionsAndAnnualLimit(LeaveRequestCreateDTO dto, Long employeeId, int maxVacaciones, long requestedDays) {
        LocalDate startOfYear = LocalDate.of(dto.getStartDate().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(dto.getStartDate().getYear(), 12, 31);

        // Obtener vacaciones del año actual (incluye pendientes y aprobadas)
        List<LeaveRequest> vacationsThisYear = leaveRequestRepository
                .findApprovedByEmployeeTypeAndDateRange(employeeId, LeaveType.VACACIONES, startOfYear, endOfYear);

        int existingSessions = vacationsThisYear.size();
        long alreadyRequestedDays = vacationsThisYear.stream()
                .mapToLong(lr -> countWorkingDays(lr.getStartDate(), lr.getEndDate()))
                .sum();

        // Validar límite de días anuales
        if (alreadyRequestedDays + requestedDays > maxVacaciones) {
            throw new ErrorSistema(String.format("No puede exceder %d días hábiles de vacaciones en el año. Ya ha solicitado: %d días",
                    maxVacaciones, alreadyRequestedDays));
        }

        // Validar límite de sesiones según antigüedad
        if (maxVacaciones == 15) {
            // Menos de 2 años: solo 1 sesión de 15 días
            if (existingSessions >= 1) {
                throw new ErrorSistema("Con su antigüedad solo puede solicitar una sesión de vacaciones de 15 días por año");
            }
        } else {
            // 2+ años: máximo 2 sesiones
            if (existingSessions >= 2) {
                throw new ErrorSistema("Ya ha alcanzado el límite máximo de 2 sesiones de vacaciones por año");
            }

            // Si ya tiene una sesión, validar que la segunda complete exactamente los días restantes
            if (existingSessions == 1) {
                long daysInFirstSession = countWorkingDays(vacationsThisYear.get(0).getStartDate(), vacationsThisYear.get(0).getEndDate());
                long remainingDays = maxVacaciones - daysInFirstSession;

                if (requestedDays != remainingDays) {
                    throw new ErrorSistema(String.format("Ya utilizó %d días en su primera sesión. La segunda sesión debe ser de exactamente %d días",
                            daysInFirstSession, remainingDays));
                }
            }
        }
    }


    private void validateEnfermedad(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 3) {
            throw new ErrorSistema("Los permisos por enfermedad no pueden exceder 3 días hábiles. Para períodos mayores debe presentar incapacidad médica");
        }

        validateAnnualLimitByType(dto, employee.getId(), LeaveType.ENFERMEDAD, 10); // máximo 10 días al año
    }

    private void validateMatrimonio(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 3) {
            throw new ErrorSistema("El permiso por matrimonio es de máximo 3 días hábiles");
        }

        // Validar que no haya usado este tipo antes
        boolean hasUsedMarriageLeave = leaveRequestRepository.existsApprovedByEmployeeAndType(
                employee.getId(), LeaveType.MATRIMONIO);

        if (hasUsedMarriageLeave) {
            throw new ErrorSistema("El permiso por matrimonio solo puede usarse una vez en la vida laboral");
        }

        // Debe solicitarse con anticipación
        if (dto.getStartDate().isBefore(LocalDate.now().plusDays(7))) {
            throw new ErrorSistema("El permiso por matrimonio debe solicitarse con al menos 7 días de anticipación");
        }
    }

    private void validateFallecimiento(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 3) {
            throw new ErrorSistema("El permiso por fallecimiento familiar es de máximo 3 días hábiles");
        }

        // Puede ser inmediato o retroactivo hasta 5 días
        if (dto.getStartDate().isBefore(LocalDate.now().minusDays(5))) {
            throw new ErrorSistema("El permiso por fallecimiento puede solicitarse hasta 5 días después del evento");
        }

        validateAnnualLimitByType(dto, employee.getId(), LeaveType.FALLECIMIENTO_FAMILIAR, 6); // máximo 2 eventos al año
    }

    private void validateNacimiento(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 3) {
            throw new ErrorSistema("El permiso por nacimiento de hijo es de máximo 3 días hábiles");
        }

        // Puede ser inmediato o hasta 30 días después del nacimiento
        if (dto.getStartDate().isBefore(LocalDate.now().minusDays(30))) {
            throw new ErrorSistema("El permiso por nacimiento debe tomarse dentro de los 30 días posteriores al evento");
        }
    }

    private void validateMudanza(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 2) {
            throw new ErrorSistema("El permiso por mudanza es de máximo 2 días hábiles");
        }

        validateAnnualLimitByType(dto, employee.getId(), LeaveType.MUDANZA, 2); // máximo 1 vez al año

        // Anticipación mínima
        if (dto.getStartDate().isBefore(LocalDate.now().plusDays(3))) {
            throw new ErrorSistema("El permiso por mudanza debe solicitarse con al menos 3 días de anticipación");
        }
    }

    private void validateCitaMedica(LeaveRequestCreateDTO dto, Employee employee) {
        long workingDaysRequested = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (workingDaysRequested > 1) {
            throw new ErrorSistema("El permiso por cita médica es de máximo 1 día");
        }

        validateAnnualLimitByType(dto, employee.getId(), LeaveType.CITA_MEDICA, 6);

        // Anticipación mínima
        if (dto.getStartDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new ErrorSistema("Las citas médicas deben solicitarse con al menos 1 día de anticipación, excepto emergencias");
        }
    }

    private void validateDateRules(LeaveRequestCreateDTO dto, Long employeeId) {
        // Fechas válidas
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ErrorSistema("Las fechas de inicio y fin son obligatorias");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new ErrorSistema("La fecha fin no puede ser anterior a la de inicio");
        }

        // No permitir solicitudes muy lejanas (más de 1 año)
        if (dto.getStartDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new ErrorSistema("No se pueden solicitar permisos con más de 1 año de anticipación");
        }

    }

    private void validateOverlap(LeaveRequestCreateDTO dto, Long employeeId) {
        // Solapamiento con permisos propios
        boolean overlap = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndDateRangeOverlap(
                        employeeId,
                        List.of(LeaveStatus.PENDIENTE_JEFE, LeaveStatus.PENDIENTE_RRHH, LeaveStatus.APROBADO),
                        dto.getStartDate(),
                        dto.getEndDate()
                );
        if (overlap) {
            throw new ErrorSistema("Ya tiene una solicitud de permiso aprobada o pendiente en esas fechas");
        }
    }

    private void validateBusinessRules(LeaveRequestCreateDTO dto, Long employeeId) {
        var employee = employeeRepository.findById(employeeId).get();

        // Regla: empleados en período de prueba no pueden tomar vacaciones
        if (dto.getType() == LeaveType.VACACIONES && isInProbationPeriod(employee)) {
            throw new ErrorSistema("Los empleados que llevan menos de 6 meses no pueden solicitar vacaciones");
        }
        // Nueva regla: solo una solicitud pendiente a la vez
        validateOnlyOnePendingRequest(employeeId);
    }

    private void validateOnlyOnePendingRequest(Long employeeId) {
        boolean hasPending = leaveRequestRepository.existsByEmployeeIdAndStatusIn(
                employeeId,
                List.of(LeaveStatus.PENDIENTE_JEFE, LeaveStatus.PENDIENTE_RRHH)
        );
        if (hasPending) {
            throw new ErrorSistema("Ya tiene una solicitud de permiso pendiente, espere a que sea aprobada o rechazada antes de crear otra.");
        }
    }

    // Métodos auxiliares
    private int calculateYearsOfService(LocalDate hireDate) {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    private boolean isInProbationPeriod(Employee employee) {
        return employee.getHireDate().isAfter(LocalDate.now().minusMonths(6));
    }

    private void validateAnnualLimit(LeaveRequestCreateDTO dto, Long employeeId, int maxDays) {
        LocalDate startOfYear = LocalDate.of(dto.getStartDate().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(dto.getStartDate().getYear(), 12, 31);

        List<LeaveRequest> approvedThisYear = leaveRequestRepository
                .findApprovedByEmployeeAndDateRange(employeeId, startOfYear, endOfYear);

        long alreadyTakenDays = approvedThisYear.stream()
                .mapToLong(lr -> countWorkingDays(lr.getStartDate(), lr.getEndDate()))
                .sum();

        long requestedDays = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (alreadyTakenDays + requestedDays > maxDays) {
            throw new ErrorSistema(String.format("No puede exceder %d días hábiles de permisos en el año. Ya ha usado: %d días",
                    maxDays, alreadyTakenDays));
        }
    }

    private void validateAnnualLimitByType(LeaveRequestCreateDTO dto, Long employeeId, LeaveType type, int maxDays) {
        LocalDate startOfYear = LocalDate.of(dto.getStartDate().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(dto.getStartDate().getYear(), 12, 31);

        List<LeaveRequest> approvedThisYear = leaveRequestRepository
                .findApprovedByEmployeeTypeAndDateRange(employeeId, type, startOfYear, endOfYear);

        long alreadyTakenDays = approvedThisYear.stream()
                .mapToLong(lr -> countWorkingDays(lr.getStartDate(), lr.getEndDate()))
                .sum();

        long requestedDays = countWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (alreadyTakenDays + requestedDays > maxDays) {
            throw new ErrorSistema(String.format("Ha excedido el límite anual para %s. Máximo: %d días, ya usados: %d días",
                    type.name().toLowerCase(), maxDays, alreadyTakenDays));
        }
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