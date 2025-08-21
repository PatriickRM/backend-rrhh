package com.rrhh.backend.web.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserStatusUpdateDTO {
    @NotNull(message = "El estado es obligatorio!!")
    private Boolean enabled;
}
