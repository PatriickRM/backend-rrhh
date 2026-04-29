package com.rrhh.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad Empleado — extiende Auditable para registrar automáticamente:
 *   created_at, updated_at, created_by, last_modified_by
 *
 * Hibernate genera las columnas de auditoría con ddl-auto: update.
 * Con Flyway, se agregarían en una migración V2__add_audit_columns.sql
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)  // importante: evita incluir campos de Auditable en equals
@Table(name = "employee")
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee")
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 8, unique = true)
    private String dni;

    @Column(nullable = false, length = 200, unique = true)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private LocalDate contractEndDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_position", nullable = false)
    private Position position;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_department", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Double salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
}
