package com.rrhh.backend.web.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatsDTO {
    private int totalRequests;
    private int approvedRequests;
    private int pendingRequests;
    private int rejectedRequests;
    private List<MonthlyStatsDTO> requestsByMonth;
    private List<TypeStatsDTO> requestsByType;
    private double averageResponseDays;
}