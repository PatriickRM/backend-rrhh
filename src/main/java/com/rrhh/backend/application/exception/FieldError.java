package com.rrhh.backend.application.exception;

/**
 * Representa un error de validación en un campo específico del request.
 */
public record FieldError(
        String campo,
        String mensaje,
        String valorRecibido
) {}
