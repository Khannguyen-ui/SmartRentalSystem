package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partners")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String serviceType; // MOVING (Chuyển nhà), CLEANING (Dọn dẹp)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean isActive;
}