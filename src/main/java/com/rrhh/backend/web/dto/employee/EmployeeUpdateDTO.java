package com.rrhh.backend.web.dto.employee;

import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.domain.model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateDTO {
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
    @NotNull(message = "La posición es obligatoria!")
    private Long positionId;

    @NotNull(message = "El departamento es obligatorio!")
    private Long departmentId;

    private Double salary;
    private EmployeeStatus status;
    private Gender gender;
}