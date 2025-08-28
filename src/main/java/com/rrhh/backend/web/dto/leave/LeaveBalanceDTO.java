package com.rrhh.backend.web.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private Long employeeId;
    private String employeeName;
    private int yearsOfService;

    // Vacaciones
    private int maxVacationDays;
    private int usedVacationDays;
    private int availableVacationDays;

}
