package com.rrhh.backend.web.controller;

import com.rrhh.backend.application.service.LeaveRequestService;
import com.rrhh.backend.web.dto.leave.LeaveHRResponseDTO;
import com.rrhh.backend.web.dto.leave.LeaveRequestHRDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chro/leave-requests")
@AllArgsConstructor
public class HRLeaveController {
    private final LeaveRequestService leaveRequestService;

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestHRDTO>> getPendingRequests() {
        List<LeaveRequestHRDTO> requests = leaveRequestService.getPendingRequestsForHR();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestHRDTO>> getAllRequests() {
        List<LeaveRequestHRDTO> requests = leaveRequestService.getAllRequestsForHR();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestHRDTO> getRequestDetail(@PathVariable Long id) {
        LeaveRequestHRDTO requestDetail = leaveRequestService.getRequestDetailForHR(id);
        return ResponseEntity.ok(requestDetail);
    }

    @PutMapping("/respond")
    public ResponseEntity<String> respondToRequest(@RequestBody LeaveHRResponseDTO responseDTO) {
        leaveRequestService.respondAsHR(responseDTO);
        return ResponseEntity.ok("Respuesta enviada exitosamente");
    }
}
