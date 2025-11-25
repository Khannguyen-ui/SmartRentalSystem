package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người muốn thuê (Khách)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private User tenant;

    // Phòng muốn xem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    // Thời gian hẹn gặp
    private LocalDateTime meetTime;

    @Column(columnDefinition = "TEXT")
    private String message; // Lời nhắn của khách

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }

    public enum Status { PENDING, CONFIRMED, CANCELLED, COMPLETED }
}