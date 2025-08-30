package com.rrhh.backend.web.dto.position;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponseDTO {
    private Long id;
    private String title;
    private BigDecimal baseSalary;
    private boolean enabled;
    private Long departmentId;
    private String departmentName;
}