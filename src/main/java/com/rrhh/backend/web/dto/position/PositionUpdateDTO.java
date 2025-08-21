package com.rrhh.backend.web.dto.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionUpdateDTO {
    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotNull(message = "El salario base es obligatorio")
    @PositiveOrZero(message = "El salario base no puede ser negativo")
    private BigDecimal baseSalary;

    @NotNull(message = "El departamento es obligatorio")
    private Long departmentId;
}
