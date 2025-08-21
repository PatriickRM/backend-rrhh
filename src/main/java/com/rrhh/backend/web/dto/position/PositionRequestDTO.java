package com.rrhh.backend.web.dto.position;

import java.math.BigDecimal;

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
public class PositionRequestDTO {
    @NotBlank(message = "El Titulo de la posición es obligatoria")
    private String title;
    @NotNull(message = "El salario base de la posición es obligatoria")
    private BigDecimal baseSalary;
    @NotNull(message = "El departamento de la posición es obligatoria")
    private Long departmentId;
}
