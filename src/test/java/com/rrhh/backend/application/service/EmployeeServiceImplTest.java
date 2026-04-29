package com.rrhh.backend.application.service;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.application.mapper.UserMapper;
import com.rrhh.backend.application.service.impl.EmployeeServiceImpl;
import com.rrhh.backend.application.validator.EmployeeValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.*;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.employee.*;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService - Tests unitarios")
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private UserService userService;
    @Mock private EmployeeMapper employeeMapper;
    @Mock private DepartmentService departmentService;
    @Mock private EmployeeStatusService employeeStatusService;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveRequestService leaveRequestService;
    @Mock private EmployeeValidator employeeValidator;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private Position positionEmployee;
    private Position positionHead;
    private Role roleEmployee;
    private Role roleHead;
    private User user;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L).name("Tecnología").code("TEC-01").enabled(true).build();

        positionEmployee = Position.builder()
                .id(1L).title("Desarrollador").enabled(true).department(department).build();

        positionHead = Position.builder()
                .id(2L).title("Jefe de Departamento").enabled(true).department(department).build();

        roleEmployee = new Role(1L, "EMPLOYEE");
        roleHead = new Role(2L, "HEAD");

        user = User.builder()
                .id(1L).username("jperez").fullName("Juan Pérez")
                .password("encoded_pass").enabled(true)
                .roles(Set.of(roleEmployee)).build();

        employee = Employee.builder()
                .id(1L).fullName("Juan Pérez").dni("12345678")
                .email("juan@empresa.com").phone("987654321")
                .address("Av. Principal 123")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .hireDate(LocalDate.now().minusYears(1))
                .contractEndDate(LocalDate.now().plusYears(2))
                .position(positionEmployee).department(department)
                .salary(2500.0).status(EmployeeStatus.CONTRATADO)
                .gender(Gender.MASCULINO).user(user).build();
    }

    // ─────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("debe crear empleado con rol EMPLOYEE cuando no es jefe")
        void debe_crear_empleado_con_rol_employee() {
            EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                    .username("jperez").password("pass123").fullName("Juan Pérez")
                    .dni("12345678").email("juan@empresa.com").phone("987654321")
                    .address("Av. Principal 123")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .hireDate(LocalDate.now().minusDays(30))
                    .contractEndDate(LocalDate.now().plusYears(2))
                    .positionId(1L).departmentId(1L)
                    .salary(2500.0).gender(Gender.MASCULINO).build();

            EmployeeResponseDTO responseDTO = EmployeeResponseDTO.builder()
                    .id(1L).fullName("Juan Pérez").positionTitle("Desarrollador").build();

            doNothing().when(employeeValidator).validateCreate(any());
            when(positionRepository.getReferenceById(1L)).thenReturn(positionEmployee);
            when(departmentRepository.getReferenceById(1L)).thenReturn(department);
            when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(roleEmployee));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
            when(userMapper.toEntity(any(UserRequestDTO.class), any(Role.class))).thenReturn(user);
            when(userRepository.save(any())).thenReturn(user);
            when(employeeMapper.toEntity(any(), any(), any(), any())).thenReturn(employee);
            when(employeeRepository.save(any())).thenReturn(employee);
            when(employeeMapper.toDto(employee)).thenReturn(responseDTO);

            EmployeeResponseDTO result = employeeService.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("Juan Pérez");
            verify(roleRepository).findByName("EMPLOYEE");
            verify(userRepository).save(any(User.class));
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("debe asignar rol HEAD y actualizar jefe de departamento si la posición es Jefe de Departamento")
        void debe_asignar_rol_head_cuando_posicion_es_jefe() {
            EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                    .username("jjefe").password("pass123").fullName("Ana López")
                    .dni("87654321").email("ana@empresa.com").phone("912345678")
                    .address("Calle Secundaria 456")
                    .dateOfBirth(LocalDate.of(1985, 3, 20))
                    .hireDate(LocalDate.now().minusDays(30))
                    .contractEndDate(LocalDate.now().plusYears(2))
                    .positionId(2L).departmentId(1L)
                    .salary(5000.0).gender(Gender.FEMENINO).build();

            Employee headEmployee = Employee.builder()
                    .id(2L).fullName("Ana López").position(positionHead)
                    .department(department).status(EmployeeStatus.CONTRATADO).build();

            EmployeeResponseDTO responseDTO = EmployeeResponseDTO.builder()
                    .id(2L).fullName("Ana López").positionTitle("Jefe de Departamento").build();

            doNothing().when(employeeValidator).validateCreate(any());
            when(positionRepository.getReferenceById(2L)).thenReturn(positionHead);
            when(departmentRepository.getReferenceById(1L)).thenReturn(department);
            when(roleRepository.findByName("HEAD")).thenReturn(Optional.of(roleHead));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
            when(userMapper.toEntity(any(), any())).thenReturn(user);
            when(userRepository.save(any())).thenReturn(user);
            when(employeeMapper.toEntity(any(), any(), any(), any())).thenReturn(headEmployee);
            when(employeeRepository.save(any())).thenReturn(headEmployee);
            when(employeeMapper.toDto(headEmployee)).thenReturn(responseDTO);
            doNothing().when(departmentService).updateHeadIfChanged(headEmployee);

            EmployeeResponseDTO result = employeeService.create(dto);

            assertThat(result).isNotNull();
            verify(roleRepository).findByName("HEAD");
            verify(departmentService).updateHeadIfChanged(headEmployee);
        }
    }

    // ─────────────────────────────────────────────
    // updateStatusToRetired
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("updateStatusToRetired()")
    class UpdateStatusToRetired {

        @Test
        @DisplayName("debe cambiar el estado del empleado a RETIRADO")
        void debe_cambiar_estado_a_retirado() {
            EmployeeResponseDTO responseDTO = EmployeeResponseDTO.builder()
                    .id(1L).status(EmployeeStatus.RETIRADO).build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any())).thenReturn(employee);
            when(employeeMapper.toDto(any())).thenReturn(responseDTO);

            EmployeeResponseDTO result = employeeService.updateStatusToRetired(1L);

            assertThat(result.getStatus()).isEqualTo(EmployeeStatus.RETIRADO);
            assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.RETIRADO);
        }

        @Test
        @DisplayName("debe lanzar error si el empleado no existe")
        void debe_lanzar_error_si_no_existe() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateStatusToRetired(999L))
                    .isInstanceOf(ErrorSistema.class)
                    .hasMessageContaining("Empleado no encontrado");
        }
    }

    // ─────────────────────────────────────────────
    // searchByName
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("searchByName()")
    class SearchByName {

        @Test
        @DisplayName("debe retornar todos si el nombre es null o vacío")
        void debe_retornar_todos_si_nombre_es_nulo() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Employee> page = new PageImpl<>(List.of(employee));

            when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(employeeMapper.toDto(any()))
                    .thenReturn(EmployeeResponseDTO.builder().id(1L).build());

            PagedResponse<EmployeeResponseDTO> result =
                    employeeService.searchByName(null, pageable);

            assertThat(result.getContent()).hasSize(1);

            verify(employeeRepository).findAll(any(Pageable.class));
            verify(employeeRepository, never())
                    .findByFullNameContainingIgnoreCase(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("debe buscar por nombre cuando se proporciona un filtro")
        void debe_buscar_por_nombre_con_filtro() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Employee> page = new PageImpl<>(List.of(employee));

            when(employeeRepository
                    .findByFullNameContainingIgnoreCase(eq("Juan"), any(Pageable.class)))
                    .thenReturn(page);

            when(employeeMapper.toDto(any()))
                    .thenReturn(EmployeeResponseDTO.builder()
                            .id(1L)
                            .fullName("Juan Pérez")
                            .build());

            PagedResponse<EmployeeResponseDTO> result =
                    employeeService.searchByName("Juan", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName())
                    .isEqualTo("Juan Pérez");
        }
    }

    // ─────────────────────────────────────────────
    // filterByStatus
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("filterByStatus()")
    class FilterByStatus {

        @Test
        @DisplayName("debe filtrar empleados correctamente por status CONTRATADO")
        void debe_filtrar_por_status() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Employee> page = new PageImpl<>(List.of(employee));

            when(employeeRepository
                    .findByStatus(eq(EmployeeStatus.CONTRATADO), any(Pageable.class)))
                    .thenReturn(page);

            when(employeeMapper.toDto(any()))
                    .thenReturn(EmployeeResponseDTO.builder()
                            .id(1L)
                            .status(EmployeeStatus.CONTRATADO)
                            .build());

            PagedResponse<EmployeeResponseDTO> result =
                    employeeService.filterByStatus(EmployeeStatus.CONTRATADO, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus())
                    .isEqualTo(EmployeeStatus.CONTRATADO);
        }
    }
}
