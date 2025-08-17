package com.rrhh.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDTO {
    @NotBlank(message="El nombre de usuario es obligatorio.")
    private String username;

    @NotBlank(message="El nombre completo es obligatorio.")
    private String fullName;

    @NotBlank(message ="La contraseña es obligatoria.")
    private String password;

    @NotNull(message = "Se debe ingresar al menos un rol!")
    private Long roleId;
}
