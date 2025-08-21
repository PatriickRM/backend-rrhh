package com.rrhh.backend.web.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUpdateDTO {
    @NotBlank(message = "El codigo debe ser obligatorio")
    @Size(max = 7, message = "El codigo debe ser de maximo 6 caracteres")
    private String code;
    private String name;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max= 64, message = "El nombre del departamento debe ser de maximo 32 caracteres")
    private String description;
}
