package com.rrhh.backend.application.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ── 1. Errores de negocio (lanzados por los services) ──────────────────
    @ExceptionHandler(ErrorSistema.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            ErrorSistema ex, WebRequest request) {

        log.warn("Error de negocio: {} — ruta: {}", ex.getMessage(), request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 2. Errores de validación (@Valid, @NotBlank, @Email…) ───
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue() != null
                                ? error.getRejectedValue().toString()
                                : null
                ))
                .collect(Collectors.toList());

        log.warn("Validación fallida en {}: {} error(es)",
                request.getDescription(false), fieldErrors.size());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(
                        "Error de validación en los datos enviados",
                        LocalDateTime.now(),
                        request.getDescription(false),
                        fieldErrors
                ));
    }

    // ── 3. JSON malformado o tipo de dato incorrecto en el request body ─────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, WebRequest request) {

        log.warn("Request con JSON inválido: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "El cuerpo de la solicitud no es JSON válido o contiene tipos de dato incorrectos",
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 4. Parámetro de query requerido faltante (?status=, ?name=…) ───────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Parámetro requerido faltante: '" + ex.getParameterName() + "'",
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 5. Tipo de parámetro incorrecto (/api/employees/abc cuando esperaba Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "desconocido";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        String.format("El parámetro '%s' debe ser de tipo %s",
                                ex.getName(), expectedType),
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 6. Archivo demasiado grande ─────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(
                        "El archivo enviado supera el tamaño máximo permitido (10 MB)",
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 7. Acceso denegado (rol insuficiente) — complementa el handler de Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Acceso denegado a '{}': {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        "No tienes permisos para realizar esta acción",
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }

    // ── 8. Cualquier otra excepción no capturada ────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        // log.error para que quede en el sistema de monitoreo
        log.error("Error inesperado en '{}': {}", request.getDescription(false), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Error interno del servidor. Por favor contacta al administrador.",
                        LocalDateTime.now(),
                        request.getDescription(false)
                ));
    }
}
