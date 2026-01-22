package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "search_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String queryText;
    // Từ khóa tìm kiếm hoặc địa chỉ người dùng nhập (VD: "Quận 1, TP.HCM")
    @Column(columnDefinition = "TEXT")
    private String address;

    // Tọa độ đã tìm (để người dùng click vào là search lại được ngay)
    private Double latitude;
    private Double longitude;
    private Double radius;

    @Column(name = "searched_at")
    private LocalDateTime searchedAt;

    @PrePersist
    protected void onCreate() {
        searchedAt = LocalDateTime.now();
    }
}