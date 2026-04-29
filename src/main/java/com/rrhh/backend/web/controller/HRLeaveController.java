package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.web.dto.common.PagedResponse;
import com.rrhh.backend.web.dto.leave.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RRHH — Solicitudes de Permiso",
     description = "Gestión final de permisos por parte de RRHH/CHRO. Segunda etapa del flujo de aprobación.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/chro/leave-requests")
@AllArgsConstructor
public class HRLeaveController {

    private final LeaveRequestService leaveRequestService;

    @Operation(summary = "Listar solicitudes pendientes de aprobación RRHH",
               description = "Devuelve solicitudes en estado PENDIENTE_RRHH — ya aprobadas por el jefe de departamento.")
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes pendientes")
    @GetMapping("/pending")
    public ResponseEntity<PagedResponse<LeaveRequestHRDTO>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Pendientes no se paginan en la query original — convertimos la lista
        var list = leaveRequestService.getPendingRequestsForHR();
        int total = list.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex   = Math.min(fromIndex + size, total);

        return ResponseEntity.ok(PagedResponse.<LeaveRequestHRDTO>builder()
                .content(list.subList(fromIndex, toIndex))
                .page(page).size(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .first(page == 0).last(toIndex >= total)
                .build());
    }

    @Operation(summary = "Listar todas las solicitudes del sistema (paginado)",
               description = "Historial completo de todas las solicitudes de todos los empleados, ordenadas por fecha descendente.")
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<LeaveRequestHRDTO>> getAllRequests(
            @Parameter(description = "Página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Registros por página") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());
        return ResponseEntity.ok(leaveRequestService.getAllRequestsForHR(pageable));
    }

    @Operation(summary = "Obtener detalle de una solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle de la solicitud"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestHRDTO> getRequestDetail(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getRequestDetailForHR(id));
    }

    @Operation(summary = "Responder como RRHH — aprobación o rechazo final",
               description = """
                   Segunda y última etapa del flujo de aprobación.
                   
                   La solicitud debe estar en estado `PENDIENTE_RRHH`.
                   
                   - Si se aprueba (`APROBADO`): el permiso queda confirmado y se notifica al empleado.
                   - Si se rechaza (`RECHAZADO`): el permiso no se otorga y se notifica al empleado.
                   """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Respuesta registrada correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud no está en estado PENDIENTE_RRHH")
    })
    @PutMapping("/respond")
    public ResponseEntity<String> respondToRequest(@RequestBody LeaveHRResponseDTO responseDTO) {
        leaveRequestService.respondAsHR(responseDTO);
        return ResponseEntity.ok("Respuesta enviada exitosamente");
    }
}
