package com.rrhh.backend.web.dto.department;



import com.rrhh.backend.web.dto.employee.EmployeeSummaryDTO;
import com.rrhh.backend.web.dto.position.PositionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean enabled;
    private EmployeeSummaryDTO head;
    private List<PositionResponseDTO> positions;
}
