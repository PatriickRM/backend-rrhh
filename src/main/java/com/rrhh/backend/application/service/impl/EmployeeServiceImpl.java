package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.application.mapper.UserMapper;
import com.rrhh.backend.application.service.*;
import com.rrhh.backend.application.validator.EmployeeValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.*;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.employee.*;
import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final EmployeeValidator employeeValidator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;
    private final EmployeeMapper employeeMapper;
    private final DepartmentService departmentService;
    private final EmployeeStatusService employeeStatusService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestService leaveRequestService;

    @Override
    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
        employeeValidator.validateCreate(dto);

        Position position = positionRepository.getReferenceById(dto.getPositionId());
        Department department = departmentRepository.getReferenceById(dto.getDepartmentId());

        String roleName = "Jefe de Departamento".equalsIgnoreCase(position.getTitle()) ? "HEAD" : "EMPLOYEE";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ErrorSistema("Rol no encontrado: " + roleName));

        UserRequestDTO userRequest = UserRequestDTO.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .build();

        User savedUser = userRepository.save(userMapper.toEntity(userRequest, role));
        Employee savedEmployee = employeeRepository.save(
                employeeMapper.toEntity(dto, savedUser, position, department));

        if ("Jefe de Departamento".equalsIgnoreCase(savedEmployee.getPosition().getTitle())) {
            departmentService.updateHeadIfChanged(savedEmployee);
        }

        log.info("Empleado creado: id={}, nombre='{}'", savedEmployee.getId(), savedEmployee.getFullName());
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    public EmployeeResponseDTO update(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado: id=" + id));
        employeeValidator.validateUpdate(dto, employee);

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ErrorSistema("Departamento no válido"));
        Position position = positionRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new ErrorSistema("Posición no válida"));

        if (employee.getUser() != null) {
            userService.updateUserRoleBasedOnPosition(employee.getUser(), position);
        }

        employeeMapper.updateEntity(employee, dto, department, position);
        departmentService.removeHeadIfChanged(employee);
        departmentService.updateHeadIfChanged(employee);

        log.info("Empleado actualizado: id={}", id);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponseDTO updateStatusToRetired(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        employee.setStatus(EmployeeStatus.RETIRADO);
        log.info("Empleado retirado: id={}", id);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        employeeStatusService.actualizarEstadoSiContratoVencido(employee);
        employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    // ── Paginados ─────────────────────────────────────────────────────────

    @Override
    public PagedResponse<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);
        // Actualizar estados de contrato vencido y persistir
        List<Employee> content = page.getContent();
        content.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(content);
        return PagedResponse.of(page.map(employeeMapper::toDto));
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> searchByName(String name, Pageable pageable) {
        Page<Employee> page = (name == null || name.isBlank())
                ? employeeRepository.findAll(pageable)
                : employeeRepository.findByFullNameContainingIgnoreCase(name, pageable);
        return PagedResponse.of(page.map(employeeMapper::toDto));
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> getByDepartment(Long departmentId, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByDepartmentId(departmentId, pageable);
        List<Employee> content = page.getContent();
        content.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(content);
        return PagedResponse.of(page.map(employeeMapper::toDto));
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> getByPosition(Long positionId, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByPositionId(positionId, pageable);
        List<Employee> content = page.getContent();
        content.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(content);
        return PagedResponse.of(page.map(employeeMapper::toDto));
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> getByHireDateRange(
            LocalDate start, LocalDate end, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByHireDateBetween(start, end, pageable);
        List<Employee> content = page.getContent();
        content.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(content);
        return PagedResponse.of(page.map(employeeMapper::toDto));
    }

    @Override
    public PagedResponse<EmployeeResponseDTO> filterByStatus(EmployeeStatus status, Pageable pageable) {
        return PagedResponse.of(
                employeeRepository.findByStatus(status, pageable).map(employeeMapper::toDto));
    }

    // ── Dashboard ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EmployeeStatsDTO getEmployeeStats(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        return employeeMapper.toStatsDto(leaveRequestRepository.findByEmployeeId(employee.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDashboardDTO getEmployeeDashboard(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        LeaveBalanceDTO leaveBalance = leaveRequestService.getEmployeeLeaveBalance(employee.getId());
        return employeeMapper.toDashboardDto(employee, leaveBalance,
                leaveRequestRepository.findByEmployeeId(employee.getId()));
    }
}
