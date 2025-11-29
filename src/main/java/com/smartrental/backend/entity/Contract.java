package com.smartrental.backend.entity;

import com.smartrental.backend.model.json.ServiceFee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private User tenant; // Người đứng tên hợp đồng

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;

    // Phí dịch vụ (Lưu snapshot lúc ký)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ServiceFee> serviceFees;

    // Giá điện nước lúc ký
    private BigDecimal electricPrice;
    private BigDecimal waterPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { PENDING, ACTIVE, EXPIRED, TERMINATED }
}