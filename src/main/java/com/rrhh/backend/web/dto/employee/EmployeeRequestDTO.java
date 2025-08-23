package com.rrhh.backend.web.dto.employee;

import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.domain.model.Gender;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {
    //Datos Usuario
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    // Datos empleado
    @NotBlank(message = "El Nombre completo es obligatorio!")
    private String fullName;
    @NotBlank(message = "El dni es obligatorio!")
    private String dni;
    @Email
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private LocalDate contractEndDate;
    @NotNull(message = "Colocar la posicion del empleado es obligatorio!")
    private Long positionId;
    @NotNull(message = "Colocar el departamento del empleado es obligatorio!")
    private Long departmentId;
    private Double salary;
    private EmployeeStatus status;
    private Gender gender;
}
