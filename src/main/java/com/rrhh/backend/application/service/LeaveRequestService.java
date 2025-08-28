package com.rrhh.backend.application.service;

import com.rrhh.backend.domain.model.Employee;
import com.rrhh.backend.web.dto.leave.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestCreateDTO createLeaveRequest(LeaveRequestCreateDTO dto, Long employeeId, MultipartFile evidenceFile);

    List<LeaveRequestEmployeeDTO> getMyLeaveRequests(Long employeeId);

    LeaveRequestEmployeeDTO getMyLeaveRequestDetail(Long requestId, Long employeeId);

    LeaveBalanceDTO getEmployeeLeaveBalance(Long employeeId);

    // JEFE
    List<LeaveRequestHeadDTO> getPendingRequestsForHead(Long headEmployeeId);

    List<LeaveRequestHeadDTO> getAllRequestsForHead(Long headEmployeeId);

    LeaveRequestHeadDTO getRequestDetailForHead(Long requestId, Long headEmployeeId);

    void respondAsHead(LeaveHeadResponseDTO responseDTO, Long headEmployeeId);

    // RRHH
    List<LeaveRequestHRDTO> getPendingRequestsForHR();

    List<LeaveRequestHRDTO> getAllRequestsForHR();

    LeaveRequestHRDTO getRequestDetailForHR(Long requestId);

    void respondAsHR(LeaveHRResponseDTO responseDTO);
}
