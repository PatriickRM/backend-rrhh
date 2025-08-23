package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.EmployeeService;
import com.rrhh.backend.domain.model.EmployeeStatus;
import com.rrhh.backend.web.dto.employee.EmployeeRequestDTO;
import com.rrhh.backend.web.dto.employee.EmployeeResponseDTO;
import com.rrhh.backend.web.dto.employee.EmployeeUpdateDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@RequestBody @Valid EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable Long id, @RequestBody @Valid EmployeeUpdateDTO dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @PutMapping("/{id}/retire")
    public ResponseEntity<EmployeeResponseDTO> updateStatusToRetired(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.updateStatusToRetired(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDTO>> searchByName(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(employeeService.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.getByDepartment(departmentId));
    }

    @GetMapping("/position/{positionId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByPosition(@PathVariable Long positionId) {
        return ResponseEntity.ok(employeeService.getByPosition(positionId));
    }

    @GetMapping("/hire-date")
    public ResponseEntity<List<EmployeeResponseDTO>> getByHireDateRange(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return ResponseEntity.ok(employeeService.getByHireDateRange(start, end));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/status")
    public ResponseEntity<List<EmployeeResponseDTO>> filterByStatus(@RequestParam EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.filterByStatus(status));
    }
}
