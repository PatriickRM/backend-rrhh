package com.rrhh.backend.web.dto.leave;

import com.rrhh.backend.domain.model.LeaveStatus;
import com.rrhh.backend.domain.model.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestHRDTO {
        private Long id;
        private String employeeName;
        private String employeeDepartment;
        private LeaveType type;
        private LeaveStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private String justification;
        private String headComment;
        private String hrComment;
        private String reviewedByHeadName;
        private LocalDateTime requestDate;
        private LocalDateTime headResponseDate;
        private LocalDateTime hrResponseDate;
}
