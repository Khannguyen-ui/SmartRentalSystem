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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price; // Giá hiển thị

    // --- BỔ SUNG TRƯỜNG BỊ THIẾU ---
    private BigDecimal deposit; // Tiền cọc
    // ------------------------------

    private Double area;      // Diện tích
    private String address;

    // --- CORE HYBRID LOGIC ---
    @Enumerated(EnumType.STRING)
    private RentalType rentalType; // WHOLE (Nguyên căn) - SHARED (Ở ghép)

    private Integer capacity; // Tổng chỗ (VD: 1 hoặc 8)

    @Builder.Default
    private Integer currentTenants = 0; // Số người đang ở

    @Enumerated(EnumType.STRING)
    private GenderConstraint genderConstraint; // MALE_ONLY, FEMALE_ONLY, MIXED
    // -------------------------

    // --- GIS & JSON ---
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location; // Tọa độ bản đồ

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> amenities;

    @Enumerated(EnumType.STRING)
    private Status status; // ACTIVE, FULL, HIDDEN

    private LocalDateTime expirationDate; // Ngày hết hạn tin đăng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id")
    private User landlord;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum RentalType { WHOLE, SHARED }
    public enum GenderConstraint { MALE_ONLY, FEMALE_ONLY, MIXED }
    public enum Status { ACTIVE, FULL, HIDDEN, EXPIRED }
}