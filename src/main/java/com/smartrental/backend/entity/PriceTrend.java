package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;

@Entity
@Table(name = "price_trends")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTrend {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tọa độ trung tâm của vùng thống kê (Point 4326)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point areaCenter;

    private Integer month;
    private Integer year;

    private BigDecimal minPrice;
    private BigDecimal avgPrice;
    private BigDecimal maxPrice;

    @Enumerated(EnumType.STRING)
    private Room.RentalType rentalType; // WHOLE hoặc SHARED [cite: 592]
}