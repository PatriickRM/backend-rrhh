package com.rrhh.backend.application.service.impl;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.application.mapper.EmployeeMapper;
import com.rrhh.backend.application.mapper.UserMapper;
import com.rrhh.backend.application.service.*;
import com.rrhh.backend.application.validator.EmployeeValidator;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.*;
import com.rrhh.backend.web.dto.employee.*;
import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;
import com.rrhh.backend.web.dto.user.UserRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {
        employeeValidator.validateCreate(dto);

        Position position = positionRepository.getReferenceById(dto.getPositionId());
        Department department = departmentRepository.getReferenceById(dto.getDepartmentId());

        //Por defecto al agregar un empleado tendra el rol empleado
        String roleName = "EMPLOYEE";
        //Si tiene la posición de "Jefe de Dep" su rol sera HEAD
        if("Jefe de Departamento".equalsIgnoreCase(position.getTitle())){
            roleName = "HEAD";
        }

        Role employeeRole = roleRepository.findByName(roleName).orElseThrow(() -> new ErrorSistema("Rol no encontrado"));

        UserRequestDTO userRequest = UserRequestDTO.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .build();
        User user = userMapper.toEntity(userRequest, employeeRole);
        User saveUser = userRepository.save(user);

        Employee employee = employeeMapper.toEntity(dto, saveUser, position, department);
        Employee saveEmployee = employeeRepository.save(employee);
        if ("Jefe de Departamento".equalsIgnoreCase(saveEmployee.getPosition().getTitle())) {
            departmentService.updateHeadIfChanged(saveEmployee);
        }
        return employeeMapper.toDto(saveEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO update(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ErrorSistema("No existe la id ingresada"));
        employeeValidator.validateUpdate(dto,employee);
        Department department = departmentRepository.findById(dto.getDepartmentId()).orElseThrow(() -> new ErrorSistema("Departamento no válido"));
        Position position = positionRepository.findById(dto.getPositionId()).orElseThrow(() -> new ErrorSistema("Posición no válida"));

        User user = employee.getUser();
        if (user != null) {
            userService.updateUserRoleBasedOnPosition(user, position);
        }

        employeeMapper.updateEntity(employee,dto,department,position);
        departmentService.removeHeadIfChanged(employee);
        departmentService.updateHeadIfChanged(employee);

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateStatusToRetired(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        employee.setStatus(EmployeeStatus.RETIRADO);

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> searchByName(String name) {
        if(name == null || name.isBlank()){
            return employeeRepository.findAll()
                    .stream()
                    .map(employeeMapper::toDto)
                    .toList();
        }
        return employeeRepository.findByFullNameContainingIgnoreCase(name)
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        employeeStatusService.actualizarEstadoSiContratoVencido(employee);
        employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getByDepartment(Long departmentId) {
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        employees.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(employees);

        return employees.stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getByPosition(Long positionId) {
        List<Employee> employees = employeeRepository.findByPositionId(positionId);
        employees.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(employees);

        return employees.stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<EmployeeResponseDTO> getByHireDateRange(LocalDate start, LocalDate end) {
        List<Employee> employees = employeeRepository.findByHireDateBetween(start,end);

        employees.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(employees);
        return employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        employees.forEach(employeeStatusService::actualizarEstadoSiContratoVencido);
        employeeRepository.saveAll(employees);

        return employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> filterByStatus(EmployeeStatus status) {
        List<Employee> employees = employeeRepository.findByStatus(status);
        return employees.stream()
                .map(employeeMapper::toDto)
                .toList();
    }
    @Override
    public EmployeeStatsDTO getEmployeeStats(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getId());
        return employeeMapper.toStatsDto(requests);
    }
    @Override
    public EmployeeDashboardDTO getEmployeeDashboard(String username) {
        // Obtener el empleado por username
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ErrorSistema("Empleado no encontrado"));

        //  Obtener las solicitudes de permiso
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getId());

        // Obtener el balance de vacaciones usando el servicio existente
        LeaveBalanceDTO leaveBalance = leaveRequestService.getEmployeeLeaveBalance(employee.getId());

        // Usar el mapper para crear el dashboard
        return employeeMapper.toDashboardDto(employee, leaveBalance, requests);
    }
}
