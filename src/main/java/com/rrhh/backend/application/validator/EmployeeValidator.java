package com.rrhh.backend.application.validator;

import com.rrhh.backend.application.exception.ErrorSistema;
import com.rrhh.backend.domain.model.Department;
import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.Position;
import com.rrhh.backend.domain.repository.DepartmentRepository;
import com.rrhh.backend.domain.repository.EmployeeRepository;
import com.rrhh.backend.domain.repository.PositionRepository;
import com.rrhh.backend.domain.repository.UserRepository;
import com.rrhh.backend.web.dto.employee.EmployeeRequestDTO;
import com.rrhh.backend.web.dto.employee.EmployeeUpdateDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class EmployeeValidator {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

public void validateCreate(EmployeeRequestDTO dto) {
    // Existencia
    if (employeeRepository.existsByDni(dto.getDni())) {
        throw new ErrorSistema("El DNI ingresado ya existe");
    }
    if (employeeRepository.existsByEmail(dto.getEmail())) {
        throw new ErrorSistema("El correo ingresado ya existe");
    }
    if (userRepository.existsByUsername(dto.getUsername())) {
        throw new ErrorSistema("El nombre de usuario ya esta en uso");
    }

    // Reglas de negocio
    if (dto.getDateOfBirth() != null && dto.getDateOfBirth().isAfter(LocalDate.now().minusYears(17))) {
        throw new ErrorSistema("El empleado debe tener al menos 17 años");
    }
    if (dto.getHireDate() != null && dto.getHireDate().isAfter(LocalDate.now())) {
        throw new ErrorSistema("La fecha de contratación no puede ser futura");
    }
    if(dto.getContractEndDate() != null && dto.getContractEndDate().isBefore(dto.getHireDate())){
        throw new ErrorSistema("La fecha de finalizacion de contrato no puede ser antes de la fecha de contrato");
    }
    if (dto.getSalary() != null && dto.getSalary() < 1200.0) {
        throw new ErrorSistema("El salario no puede ser menor al mínimo legal");
    }

    // Departamento & Posiciones
    if (!departmentRepository.existsById(dto.getDepartmentId())) {
        throw new ErrorSistema("El departamento seleccionado no existe");
    }
    if (!positionRepository.existsById(dto.getPositionId())) {
        throw new ErrorSistema("La posición seleccionada no existe");
    }

    // Evitar duplicado de Jefe de departamento
    Position position = positionRepository.getReferenceById(dto.getPositionId());
    if ("Jefe de Departamento".equalsIgnoreCase(position.getTitle())) {
        Department department = departmentRepository.getReferenceById(dto.getDepartmentId());
        if (department.getHead() != null) {
            throw new ErrorSistema("Este departamento ya tiene un jefe asignado");
        }
      }
    }
    public void validateUpdate(EmployeeUpdateDTO dto, Employee employee){
            // 1. Validar edad mínima
            if (dto.getDateOfBirth() != null && dto.getDateOfBirth().isAfter(LocalDate.now().minusYears(17))) {
                throw new ErrorSistema("El empleado debe tener al menos 17 años");
            }

            if (dto.getHireDate() != null && dto.getHireDate().isAfter(LocalDate.now())) {
                throw new ErrorSistema("La fecha de contratación no puede ser futura");
            }
            if(dto.getContractEndDate() != null && dto.getContractEndDate().isBefore(dto.getHireDate())){
                throw new ErrorSistema("La fecha de finalizacion de contrato no puede ser antes de la fecha de contrato");
            }


            if (dto.getSalary() != null && dto.getSalary() < 1200.0) {
                throw new ErrorSistema("El salario no puede ser menor al mínimo legal");
            }

            if (!departmentRepository.existsById(dto.getDepartmentId())) {
                throw new ErrorSistema("El departamento seleccionado no existe");
            }
            if (!positionRepository.existsById(dto.getPositionId())) {
                throw new ErrorSistema("La posición seleccionada no existe");
            }
            if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(employee.getEmail())) {
                if (employeeRepository.existsByEmail(dto.getEmail())) {
                    throw new ErrorSistema("El correo ingresado ya existe");
                }
            }
            if (dto.getDni() != null && !dto.getDni().equalsIgnoreCase(employee.getDni())) {
                if (employeeRepository.existsByDni(dto.getDni())) {
                    throw new ErrorSistema("El DNI ingresado ya existe");
                }
            }
            Position position = positionRepository.getReferenceById(dto.getPositionId());
            if ("Jefe de Departamento".equalsIgnoreCase(position.getTitle())) {
                Department department = departmentRepository.getReferenceById(dto.getDepartmentId());
                Employee currentHead = department.getHead();

                // Solo lanzar error si hay un jefe distinto al empleado que estamos editando
                if (currentHead != null && !currentHead.getId().equals(employee.getId())) {
                    throw new ErrorSistema("Este departamento ya tiene un jefe asignado");
                }
            }
    }
}