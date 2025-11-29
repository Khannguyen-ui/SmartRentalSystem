package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private int month;
    private int year;

    // Chỉ số cũ - mới
    private int electricOld;
    private int electricNew;
    private int waterOld;
    private int waterNew;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { UNPAID, PARTIAL, PAID }
}