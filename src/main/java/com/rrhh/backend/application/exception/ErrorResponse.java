package com.rrhh.backend.application.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String mensaje, LocalDateTime fecha, String rutaError) {
}
