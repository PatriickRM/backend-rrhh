package com.rrhh.backend.application.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        String mensaje,
        LocalDateTime fecha,
        String rutaError,
        List<FieldError> errores
) {}
