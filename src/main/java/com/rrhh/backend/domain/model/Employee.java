package com.rrhh.backend.domain.model;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@Table(name="employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_employee")
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="id_user", nullable = false )
    private User user;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false,length = 8, unique = true)
    private String dni;

    @Column(nullable = false,length = 200, unique = true)
    private String email;

    @Column(nullable = false,length = 15)
    private String phone;

    @Column(nullable = false,length = 200)
    private String address;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate hireDate;

    @ManyToOne(optional = false)
    @JoinColumn(name="id_position", nullable = false)
    private Position position;

    @ManyToOne(optional = false)
    @JoinColumn(name="id_department",nullable = false)
    private Department department;

    @Column(nullable = false)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = true)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

}