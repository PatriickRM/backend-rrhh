package com.rrhh.backend.web.dto.employee;

import com.rrhh.backend.web.dto.leave.LeaveBalanceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardDTO {
    private EmployeeResponseDTO employee;
    private LeaveBalanceDTO leaveBalance;
    private EmployeeStatsDTO stats;
}