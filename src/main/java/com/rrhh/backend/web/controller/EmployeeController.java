package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.EmployeeService;
import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.employee.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Empleados", description = "Gestión del registro de empleados — solo CHRO")
@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Registrar nuevo empleado",
               description = "Crea el empleado y su usuario asociado. Si la posición es 'Jefe de Departamento', asigna rol HEAD automáticamente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empleado creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o DNI/email/username duplicado")
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@RequestBody @Valid EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.create(dto));
    }

    @Operation(summary = "Actualizar datos del empleado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empleado actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @Operation(summary = "Retirar empleado", description = "Cambia el estado a RETIRADO. Acción irreversible.")
    @PutMapping("/{id}/retire")
    public ResponseEntity<EmployeeResponseDTO> retire(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.updateStatusToRetired(id));
    }

    @Operation(summary = "Obtener empleado por ID")
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @Operation(summary = "Listar todos los empleados (paginado)",
               description = "Soporta paginación y ordenamiento. Parámetros: page (0-based), size, sort (ej: fullName,asc)")
    @GetMapping
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> getAllEmployees(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento (ej: fullName,asc)") @RequestParam(defaultValue = "fullName,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @Operation(summary = "Buscar empleados por nombre (paginado)")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> searchByName(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(employeeService.searchByName(name, pageable));
    }

    @Operation(summary = "Listar empleados por departamento (paginado)")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> getByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(employeeService.getByDepartment(departmentId, pageable));
    }

    @Operation(summary = "Listar empleados por posición (paginado)")
    @GetMapping("/position/{positionId}")
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> getByPosition(
            @PathVariable Long positionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(employeeService.getByPosition(positionId, pageable));
    }

    @Operation(summary = "Listar empleados por rango de fecha de contratación (paginado)")
    @GetMapping("/hire-date")
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> getByHireDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("hireDate").descending());
        return ResponseEntity.ok(employeeService.getByHireDateRange(start, end, pageable));
    }

    @Operation(summary = "Filtrar empleados por estado (paginado)",
               description = "Estados disponibles: CONTRATADO, FINALIZADO, RETIRADO")
    @GetMapping("/status")
    public ResponseEntity<PagedResponse<EmployeeResponseDTO>> filterByStatus(
            @RequestParam EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return ResponseEntity.ok(employeeService.filterByStatus(status, pageable));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
