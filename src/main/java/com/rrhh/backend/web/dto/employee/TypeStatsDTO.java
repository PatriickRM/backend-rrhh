package com.rrhh.backend.web.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeStatsDTO {
    private String type;
    private int count;
    private double percentage;
}