package com.rrhh.backend.web.dto.leave;

import com.rrhh.backend.domain.model.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveHeadResponseDTO {
    private Long requestId;
    private LeaveStatus status;
    private String comment;
}
