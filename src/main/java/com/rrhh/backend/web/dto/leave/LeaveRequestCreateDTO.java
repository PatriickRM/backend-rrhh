package com.rrhh.backend.web.dto.leave;

import com.rrhh.backend.domain.model.LeaveType;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestCreateDTO {
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String justification;
    private String evidenceImagePath;
}