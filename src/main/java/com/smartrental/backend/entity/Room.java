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
    private BigDecimal deposit; // Tiền cọc

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

    // --- GIS & MEDIA ---
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location; // Tọa độ bản đồ

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images;

    // (BỔ SUNG LẠI) Link Video Cloudinary
    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> amenities;

    // --- TRẠNG THÁI & GÓI CƯỚC ---

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, ACTIVE...

    private LocalDateTime expirationDate; // Ngày hết hạn tin đăng

    @Column(name = "service_package_id")
    private Integer servicePackageId; // Gói cước đã mua

    // --- QUAN HỆ ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id")
    private User landlord;

    // (BỔ SUNG LẠI) Quan hệ để lấy đánh giá
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }

    // Hàm tiện ích: Tính điểm đánh giá trung bình
    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (Review r : reviews) sum += r.getRating();
        return sum / reviews.size();
    }

    // --- CÁC ENUM ---
    public enum RentalType { WHOLE, SHARED }
    public enum GenderConstraint { MALE_ONLY, FEMALE_ONLY, MIXED }

    // (ĐÃ SỬA) Thêm PENDING vào đây để hết lỗi
    public enum Status { PENDING, ACTIVE, FULL, HIDDEN, EXPIRED,APPROVED, REJECTED, }
}