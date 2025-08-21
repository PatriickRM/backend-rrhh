package com.rrhh.backend.web.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String positionName;
}
