package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service_packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name; // VD: Gói Tuần, Gói VIP

    @Column(nullable = false)
    private BigDecimal price; // VD: 20000

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays; // VD: 7

    @Column(name = "priority_level")
    private Integer priorityLevel; // 1: Thường, 5: VIP
}