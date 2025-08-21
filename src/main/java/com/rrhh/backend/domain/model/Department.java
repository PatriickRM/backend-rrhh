package com.rrhh.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id_department")
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @OneToOne
    @JoinColumn(name="id_head_employee")
    private Employee head;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Position> positions;
}
