package com.rrhh.backend.web.dto.employee;

import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.domain.model.Gender;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String fullName;
    private Long userId;
    private String username;
    private String dni;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private LocalDate contractEndDate;
    private String positionTitle;
    private String departmentName;
    private Double salary;
    private EmployeeStatus status;
    private Gender gender;
}