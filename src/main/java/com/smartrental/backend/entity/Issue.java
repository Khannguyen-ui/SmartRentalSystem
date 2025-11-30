package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract; // Sự cố gắn liền với hợp đồng nào

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl; // Link ảnh từ Cloudinary

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, PROCESSING, DONE

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
    }

    public enum Status { PENDING, PROCESSING, DONE }
}