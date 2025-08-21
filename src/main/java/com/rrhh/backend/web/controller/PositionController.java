package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.PositionService;
import com.rrhh.backend.web.dto.position.PositionRequestDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;
import com.rrhh.backend.web.dto.position.PositionStatusUpdateDTO;
import com.rrhh.backend.web.dto.position.PositionUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<PositionResponseDTO> create(@Valid @RequestBody PositionRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(dto));
    }

    @GetMapping
    public List<PositionResponseDTO> getAllPositions(){
        return positionService.getAllPositions();
    }

    @GetMapping("/{id}")
    public PositionResponseDTO getById(@PathVariable Long id){
        return positionService.getPositionById(id);
    }

    // TODO: Implementar mejor logica al update con el departmentId y PositionId
    @PutMapping("/{id}")
    public PositionResponseDTO update(@PathVariable Long id, @Valid @RequestBody PositionUpdateDTO dto){
        return positionService.updatePosition(id,dto);
    }

    @PatchMapping("/{id}/status")
    public PositionResponseDTO updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto){
        return positionService.updatePositionStatus(id,dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PositionResponseDTO>> searchPositionsByName(
            @RequestParam(name = "title", required = false) String titleFilter) {
        return ResponseEntity.ok(positionService.getPositionsByName(titleFilter));
    }
    @PostMapping("/batch")
    public ResponseEntity<?> createDepartments(@RequestBody List<PositionRequestDTO> dto) {
        dto.forEach(positionService::create);
        return ResponseEntity.ok(Map.of("message", "Posiciones creadas correctamente")); //mostrar respuesta en el body
    }
    //Buscar posiciones por departamento
    @GetMapping("/dep/{departmentId}")
    public ResponseEntity<List<PositionResponseDTO>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(positionService.getPositionsByDepartment(departmentId));
    }
}
