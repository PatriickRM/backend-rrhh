package com.rrhh.backend.application.service;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.validator.LeaveRequestValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import com.rrhh.backend.web.dto.leave.LeaveRequestCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveRequestValidator - Tests unitarios")
class LeaveRequestValidatorTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private LeaveRequestValidator validator;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Department department = Department.builder()
                .id(1L).name("TI").code("TI-01").enabled(true).build();

        Position position = Position.builder()
                .id(1L).title("Desarrollador").department(department).build();

        employee = Employee.builder()
                .id(1L).fullName("Test User").dni("12345678")
                .email("test@empresa.com")
                .hireDate(LocalDate.now().minusYears(2))
                .contractEndDate(LocalDate.now().plusYears(1))
                .status(EmployeeStatus.CONTRATADO)
                .position(position).department(department)
                .gender(Gender.MASCULINO).build();
    }

    private void mockNoOverlap() {
        when(leaveRequestRepository.existsByEmployeeIdAndStatusInAndDateRangeOverlap(
                any(), any(), any(), any())).thenReturn(false);
        when(leaveRequestRepository.existsByEmployeeIdAndStatusIn(any(), any())).thenReturn(false);
    }

    // ─────────────────────────────────────────────
    // Validaciones básicas
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Validaciones básicas")
    class ValidacionesBasicas {

        @Test
        @DisplayName("debe rechazar empleado retirado")
        void debe_rechazar_empleado_retirado() {
            employee.setStatus(EmployeeStatus.RETIRADO);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(3))
                    .justification("Cita médica de control")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("debe rechazar justificación vacía")
        void debe_rechazar_justificacion_vacia() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(3))
                    .justification("")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("justificación");
        }

        @Test
        @DisplayName("debe rechazar justificación menor a 10 caracteres")
        void debe_rechazar_justificacion_corta() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(3))
                    .justification("Corta")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("10 caracteres");
        }
    }

    // ─────────────────────────────────────────────
    // Cita médica
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Validaciones de CITA_MEDICA")
    class CitaMedica {

        @Test
        @DisplayName("debe rechazar cita médica si se solicita para el mismo día")
        void debe_rechazar_cita_sin_anticipacion() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .justification("Cita médica de emergencia urgente")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("anticipación");
        }

        @Test
        @DisplayName("debe rechazar cita médica de más de 1 día")
        void debe_rechazar_cita_de_mas_de_un_dia() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findApprovedByEmployeeTypeAndDateRange(any(), any(), any(), any()))
                    .thenReturn(List.of());

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(3))  // 2 días hábiles
                    .justification("Cita médica de control anual")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("máximo 1 día");
        }
    }

    // ─────────────────────────────────────────────
    // Vacaciones
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Validaciones de VACACIONES")
    class Vacaciones {

        @Test
        @DisplayName("debe rechazar vacaciones si el empleado tiene menos de 1 año de antigüedad")
        void debe_rechazar_si_menos_de_un_anio() {
            employee.setHireDate(LocalDate.now().minusMonths(6));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.VACACIONES)
                    .startDate(LocalDate.now().plusDays(20))
                    .endDate(LocalDate.now().plusDays(40))
                    .justification("Vacaciones de verano planificadas con anticipación")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("1 año de antigüedad");
        }

        @Test
        @DisplayName("debe rechazar vacaciones sin anticipación de 15 días")
        void debe_rechazar_sin_anticipacion_de_15_dias() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.VACACIONES)
                    .startDate(LocalDate.now().plusDays(5))  // menos de 15 días
                    .endDate(LocalDate.now().plusDays(25))
                    .justification("Solicitud de vacaciones anuales programadas")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("15 días de anticipación");
        }

        @Test
        @DisplayName("debe rechazar vacaciones en período de prueba (menos de 6 meses)")
        void debe_rechazar_en_periodo_de_prueba() {
            employee.setHireDate(LocalDate.now().minusMonths(3));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.VACACIONES)
                    .startDate(LocalDate.now().plusDays(20))
                    .endDate(LocalDate.now().plusDays(40))
                    .justification("Vacaciones solicitadas en período inicial")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class);
        }
    }

    // ─────────────────────────────────────────────
    // Fechas
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Validaciones de fechas")
    class Fechas {

        @Test
        @DisplayName("debe rechazar si fecha fin es anterior a fecha inicio")
        void debe_rechazar_si_fin_antes_que_inicio() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(2))  // fin antes del inicio
                    .justification("Cita médica de control rutinario")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("debe rechazar solicitud con más de 1 año de anticipación")
        void debe_rechazar_si_mas_de_un_anio_anticipacion() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusYears(2))
                    .endDate(LocalDate.now().plusYears(2))
                    .justification("Cita médica planificada con mucha anticipación")
                    .build();

            assertThatThrownBy(() -> validator.validateCreate(dto, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("1 año de anticipación");
        }
    }
}
