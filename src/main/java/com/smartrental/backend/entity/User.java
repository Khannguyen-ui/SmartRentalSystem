package com.smartrental.backend.entity;

import com.smartrental.backend.model.json.LifestyleProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String fullName;
    private String phone;

    private String avatarUrl; // Link ảnh đại diện

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, LANDLORD, TENANT

    // --- JSONB: Profile Lối sống (Cho Matching) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private LifestyleProfile lifestyleProfile;

    // --- Tài chính & Định danh ---
    @Builder.Default
    private BigDecimal walletBalance = BigDecimal.ZERO;

    private String citizenId; // Số CCCD

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> citizenImages; // Ảnh CCCD 2 mặt

    private String kycStatus; // VERIFIED, PENDING...

    // --- (MỚI) TRẠNG THÁI HOẠT ĐỘNG CHO ADMIN QUẢN LÝ ---
    @Column(name = "is_active")
    private boolean isActive; // true = Hoạt động, false = Bị khóa
    // ----------------------------------------------------

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (kycStatus == null) kycStatus = "UNVERIFIED";
        isActive = true; // Mặc định khi tạo mới là Active (Hoạt động)
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // Cập nhật hàm này để Spring Security tự động chặn đăng nhập nếu bị khóa
    @Override
    public boolean isEnabled() { return isActive; }

    public enum Role { ADMIN, LANDLORD, TENANT }
}