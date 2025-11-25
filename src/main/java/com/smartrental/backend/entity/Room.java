package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Double area;

    private String address;

    // --- POSTGIS: Lưu tọa độ ---
    // SRID 4326 là chuẩn WGS84 (Google Maps)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // --- JSONB: Lưu danh sách ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images; // Tự động map List Java <-> JSON Array

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> amenities;

    // Giới tính cho thuê (MALE, FEMALE, ALL)
    private String gender;

    private BigDecimal deposit; // Tiền cọc

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id")
    private User landlord;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private LocalDateTime expirationDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }

    public enum Status {
        PENDING, ACTIVE, RENTED, EXPIRED, HIDDEN
    }
}
