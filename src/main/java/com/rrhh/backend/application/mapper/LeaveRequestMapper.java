package com.rrhh.backend.application.mapper;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.domain.model.LeaveRequest;
import com.rrhh.backend.web.dto.leave.*;

import java.util.List;

public interface LeaveRequestMapper {
    // Request
    LeaveRequest toEntity(LeaveRequestCreateDTO dto, Employee employee, String evidenceImagePath);
    // Response
    LeaveRequestEmployeeDTO toEmployeeDTO(LeaveRequest entity);
    LeaveRequestHeadDTO toHeadDTO(LeaveRequest entity);
    LeaveRequestHRDTO toHRDTO(LeaveRequest entity);
    //Respuestas del Head o empleado
    void applyHeadResponse(LeaveRequest entity, LeaveHeadResponseDTO dto, Employee head);
    void applyHRResponse(LeaveRequest entity, LeaveHRResponseDTO dto, Employee hr);
    //Listas de solicitudes
    List<LeaveRequestEmployeeDTO> toEmployeeDTOs(List<LeaveRequest> entities);
    List<LeaveRequestHeadDTO> toHeadDTOs(List<LeaveRequest> entities);
    List<LeaveRequestHRDTO> toHRDTOs(List<LeaveRequest> entities);
}
