package com.rrhh.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LeaveRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private LeaveType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status;

    @Column(nullable = false, length = 500)
    private String justification;

    @Column(length = 300)
    private String evidenceImagePath;

    @Column(length = 150)
    private String reviewedByHeadName;

    @Column(length = 400)
    private String headComment;

    @Column(length = 400)
    private String hrComment;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    private LocalDateTime headResponseDate;
    private LocalDateTime hrResponseDate;
}
