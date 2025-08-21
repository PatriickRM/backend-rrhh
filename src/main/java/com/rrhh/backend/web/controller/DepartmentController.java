package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.DepartmentService;
import com.rrhh.backend.web.dto.department.DepartmentRequestDTO;
import com.rrhh.backend.web.dto.department.DepartmentResponseDTO;
import com.rrhh.backend.web.dto.department.DepartmentStatusUpdateDTO;
import com.rrhh.backend.web.dto.department.DepartmentUpdateDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@AllArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> create(@Valid @RequestBody DepartmentRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(dto));
    }
    @GetMapping
    public List<DepartmentResponseDTO> getAllDepartments(){return departmentService.getAllDepartments();}

    @GetMapping("/{id}")
    public DepartmentResponseDTO getById(@PathVariable Long id){return departmentService.getDepartmentById(id);}

    @PutMapping("/{id}")
    public DepartmentResponseDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateDTO dto){
        return departmentService.updateDepartment(id,dto);
    }

    @PatchMapping("/{id}/status")
    public DepartmentResponseDTO updateStatus(@PathVariable Long id, @Valid @RequestBody DepartmentStatusUpdateDTO dto){
        return departmentService.updateStatus(id,dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DepartmentResponseDTO>> searchDepartment(@RequestParam(name = "name", required = false) String name){
        return ResponseEntity.ok(departmentService.findByName(name));
    }
    //Crear departamentos en lote
    @PostMapping("/batch")
    public ResponseEntity<?> createDepartments(@RequestBody List<DepartmentRequestDTO> dto) {
        dto.forEach(departmentService::create);
        return ResponseEntity.ok(Map.of("message", "Departamentos creados correctamente")); //mostrar respuesta en el body
    }
}
