package com.rrhh.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="position")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_position")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @ManyToOne
    @JoinColumn(name="id_department", nullable = false)
    private Department department;
}
