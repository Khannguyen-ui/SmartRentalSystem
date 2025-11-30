package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "market_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Người bán

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price; // 0 = Tặng
    private String category;  // FURNITURE, ELECTRONIC...

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images; // List ảnh Cloudinary

    private String status; // AVAILABLE, SOLD

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "AVAILABLE";
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

}