package com.rrhh.backend.web.dto.position;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PositionStatusUpdateDTO {
    @NotNull(message = "El estado es obligatorio!!")
    private Boolean enabled;
}
