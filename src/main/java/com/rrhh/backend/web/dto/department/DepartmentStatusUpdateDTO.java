package com.rrhh.backend.web.dto.department;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DepartmentStatusUpdateDTO {
    @NotNull(message = "El estado del departamento es obligatorio!")
    private Boolean enabled;
}
