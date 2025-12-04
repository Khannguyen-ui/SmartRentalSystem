package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount; // Số tiền (+ là nạp, - là mua gói)

    private String type; // DEPOSIT (Nạp tiền), POST_FEE (Phí đăng tin)

    @Column(name = "vnpay_code")
    private String vnpayCode; // Mã giao dịch trả về từ VNPay

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon; // Mã giảm giá (nếu có, có thể để null)

    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}