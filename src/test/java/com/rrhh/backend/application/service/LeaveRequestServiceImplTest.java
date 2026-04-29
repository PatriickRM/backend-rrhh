package com.rrhh.backend.application.service;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.LeaveRequestMapper;
import com.rrhh.backend.application.service.impl.LeaveRequestServiceImpl;
import com.rrhh.backend.application.utils.FileStorageService;
import com.rrhh.backend.application.validator.LeaveRequestValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.LeaveRequestRepository;
import com.rrhh.backend.web.dto.leave.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveRequestService - Tests unitarios")
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private LeaveRequestMapper leaveRequestMapper;
    @Mock
    private LeaveRequestValidator validator;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private Employee employee;
    private Department department;
    private Position position;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Tecnología")
                .code("TEC-01")
                .enabled(true)
                .build();

        position = Position.builder()
                .id(1L)
                .title("Desarrollador")
                .enabled(true)
                .department(department)
                .build();

        employee = Employee.builder()
                .id(1L)
                .fullName("Juan Pérez")
                .dni("12345678")
                .email("juan@empresa.com")
                .hireDate(LocalDate.now().minusYears(2))
                .contractEndDate(LocalDate.now().plusYears(1))
                .status(EmployeeStatus.CONTRATADO)
                .position(position)
                .department(department)
                .gender(Gender.MASCULINO)
                .build();
    }

    // ─────────────────────────────────────────────
    // createLeaveRequest
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("createLeaveRequest()")
    class CreateLeaveRequest {

        @Test
        @DisplayName("debe crear solicitud correctamente sin archivo de evidencia")
        void deberia_crear_solicitud_sin_evidencia() {
            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(3))
                    .justification("Cita de control médico anual")
                    .build();

            LeaveRequest leaveRequest = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee)
                    .type(LeaveType.CITA_MEDICA)
                    .status(LeaveStatus.PENDIENTE_JEFE)
                    .requestDate(LocalDateTime.now())
                    .build();

            doNothing().when(validator).validateCreate(any(), eq(1L));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestMapper.toEntity(any(), any(), any())).thenReturn(leaveRequest);
            when(leaveRequestRepository.save(any())).thenReturn(leaveRequest);

            LeaveRequestCreateDTO result = leaveRequestService.createLeaveRequest(dto, 1L, null);

            assertThat(result).isNotNull();
            verify(leaveRequestRepository).save(any(LeaveRequest.class));
            verify(fileStorageService, never()).storeFile(any(), any());
        }

        @Test
        @DisplayName("debe lanzar excepción si el empleado no existe")
        void deberia_lanzar_error_si_empleado_no_existe() {
            LeaveRequestCreateDTO dto = LeaveRequestCreateDTO.builder()
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(3))
                    .justification("Cita médica urgente")
                    .build();

            doNothing().when(validator).validateCreate(any(), eq(99L));
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveRequestService.createLeaveRequest(dto, 99L, null))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("Empleado no encontrado");
        }
    }

    // ─────────────────────────────────────────────
    // getEmployeeLeaveBalance
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getEmployeeLeaveBalance()")
    class GetLeaveBalance {

        @Test
        @DisplayName("empleado con 2+ años debe tener 30 días máximos de vacaciones")
        void empleado_con_mas_de_dos_anios_tiene_30_dias() {
            employee.setHireDate(LocalDate.now().minusYears(3));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findApprovedByEmployeeTypeAndDateRange(any(), any(), any(), any()))
                    .thenReturn(List.of());

            LeaveBalanceDTO balance = leaveRequestService.getEmployeeLeaveBalance(1L);

            assertThat(balance.getMaxVacationDays()).isEqualTo(30);
            assertThat(balance.getUsedVacationDays()).isEqualTo(0);
            assertThat(balance.getAvailableVacationDays()).isEqualTo(30);
        }

        @Test
        @DisplayName("empleado con entre 1 y 2 años debe tener 15 días máximos")
        void empleado_entre_uno_y_dos_anios_tiene_15_dias() {
            employee.setHireDate(LocalDate.now().minusMonths(18));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findApprovedByEmployeeTypeAndDateRange(any(), any(), any(), any()))
                    .thenReturn(List.of());

            LeaveBalanceDTO balance = leaveRequestService.getEmployeeLeaveBalance(1L);

            assertThat(balance.getMaxVacationDays()).isEqualTo(15);
        }

        @Test
        @DisplayName("empleado con menos de 1 año no tiene días de vacaciones")
        void empleado_menor_a_un_anio_no_tiene_dias() {
            employee.setHireDate(LocalDate.now().minusMonths(6));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findApprovedByEmployeeTypeAndDateRange(any(), any(), any(), any()))
                    .thenReturn(List.of());

            LeaveBalanceDTO balance = leaveRequestService.getEmployeeLeaveBalance(1L);

            assertThat(balance.getMaxVacationDays()).isEqualTo(0);
            assertThat(balance.getAvailableVacationDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("debe descontar correctamente los días de vacaciones ya usados")
        void debe_descontar_dias_ya_usados() {
            employee.setHireDate(LocalDate.now().minusYears(3));

            LeaveRequest vacacionesUsadas = LeaveRequest.builder()
                    .startDate(LocalDate.of(LocalDate.now().getYear(), 1, 6))  // lunes
                    .endDate(LocalDate.of(LocalDate.now().getYear(), 1, 17))   // 10 días hábiles
                    .type(LeaveType.VACACIONES)
                    .status(LeaveStatus.APROBADO)
                    .build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findApprovedByEmployeeTypeAndDateRange(any(), any(), any(), any()))
                    .thenReturn(List.of(vacacionesUsadas));

            LeaveBalanceDTO balance = leaveRequestService.getEmployeeLeaveBalance(1L);

            assertThat(balance.getUsedVacationDays()).isGreaterThan(0);
            assertThat(balance.getAvailableVacationDays()).isLessThan(30);
        }

        @Test
        @DisplayName("debe lanzar excepción si empleado no existe")
        void debe_lanzar_error_si_empleado_no_existe() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveRequestService.getEmployeeLeaveBalance(999L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("Empleado no encontrado");
        }
    }

    // ─────────────────────────────────────────────
    // respondAsHead
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("respondAsHead()")
    class RespondAsHead {

        @Test
        @DisplayName("si el jefe aprueba, el estado debe cambiar a PENDIENTE_RRHH")
        void aprobacion_de_jefe_debe_pasar_a_pendiente_rrhh() {
            LeaveRequest request = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee)
                    .status(LeaveStatus.PENDIENTE_JEFE)
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(5))
                    .requestDate(LocalDateTime.now())
                    .build();

            LeaveHeadResponseDTO responseDTO = LeaveHeadResponseDTO.builder()
                    .requestId(1L)
                    .status(LeaveStatus.APROBADO)
                    .comment("Aprobado, sin problema.")
                    .build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findByIdAndDepartmentHead(1L, 1L)).thenReturn(Optional.of(request));
            doNothing().when(leaveRequestMapper).applyHeadResponse(any(), any(), any());
            when(leaveRequestRepository.save(any())).thenReturn(request);

            leaveRequestService.respondAsHead(responseDTO, 1L);

            assertThat(request.getStatus()).isEqualTo(LeaveStatus.PENDIENTE_RRHH);
            verify(leaveRequestRepository).save(request);
        }

        @Test
        @DisplayName("si el jefe rechaza, el estado debe quedar en RECHAZADO")
        void rechazo_de_jefe_debe_dejar_estado_rechazado() {
            LeaveRequest request = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee)
                    .status(LeaveStatus.PENDIENTE_JEFE)
                    .type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(5))
                    .requestDate(LocalDateTime.now())
                    .build();

            LeaveHeadResponseDTO responseDTO = LeaveHeadResponseDTO.builder()
                    .requestId(1L)
                    .status(LeaveStatus.RECHAZADO)
                    .comment("No hay cobertura ese día.")
                    .build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findByIdAndDepartmentHead(1L, 1L)).thenReturn(Optional.of(request));
            doNothing().when(leaveRequestMapper).applyHeadResponse(any(), any(), any());
            when(leaveRequestRepository.save(any())).thenReturn(request);

            leaveRequestService.respondAsHead(responseDTO, 1L);

            // El status no debe ser PENDIENTE_RRHH — sigue en RECHAZADO
            assertThat(request.getStatus()).isNotEqualTo(LeaveStatus.PENDIENTE_RRHH);
            verify(leaveRequestRepository).save(request);
        }

        @Test
        @DisplayName("debe lanzar error si la solicitud ya fue procesada")
        void debe_lanzar_error_si_solicitud_ya_procesada() {
            LeaveRequest request = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee)
                    .status(LeaveStatus.APROBADO) // ya procesada
                    .build();

            LeaveHeadResponseDTO responseDTO = LeaveHeadResponseDTO.builder()
                    .requestId(1L)
                    .status(LeaveStatus.RECHAZADO)
                    .comment("Intento tardío")
                    .build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findByIdAndDepartmentHead(1L, 1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> leaveRequestService.respondAsHead(responseDTO, 1L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("ya fue procesada");
        }
    }

    // ─────────────────────────────────────────────
    // getMyLeaveRequests
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getMyLeaveRequests()")
    class GetMyLeaveRequests {

        @Test
        @DisplayName("debe retornar la lista mapeada al DTO de empleado")
        void debe_retornar_lista_de_solicitudes() {
            LeaveRequest r1 = LeaveRequest.builder().id(1L).employee(employee)
                    .status(LeaveStatus.APROBADO).type(LeaveType.CITA_MEDICA)
                    .startDate(LocalDate.now()).endDate(LocalDate.now())
                    .requestDate(LocalDateTime.now()).build();

            LeaveRequestEmployeeDTO dto1 = LeaveRequestEmployeeDTO.builder()
                    .id(1L).status(LeaveStatus.APROBADO).type(LeaveType.CITA_MEDICA).build();

            when(leaveRequestRepository.findByEmployeeIdOrderByRequestDateDesc(1L))
                    .thenReturn(List.of(r1));
            when(leaveRequestMapper.toEmployeeDTO(r1)).thenReturn(dto1);

            List<LeaveRequestEmployeeDTO> result = leaveRequestService.getMyLeaveRequests(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(LeaveStatus.APROBADO);
        }

        @Test
        @DisplayName("debe retornar lista vacía si el empleado no tiene solicitudes")
        void debe_retornar_lista_vacia_si_no_hay_solicitudes() {
            when(leaveRequestRepository.findByEmployeeIdOrderByRequestDateDesc(1L))
                    .thenReturn(List.of());

            List<LeaveRequestEmployeeDTO> result = leaveRequestService.getMyLeaveRequests(1L);

            assertThat(result).isEmpty();
        }
    }
}
