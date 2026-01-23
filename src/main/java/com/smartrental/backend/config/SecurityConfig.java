package com.smartrental.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    
    // Spring sẽ tự động tìm AuthenticationProvider từ bên AppConfig sang đây
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Các API công khai hoàn toàn
                        .requestMatchers("/api/auth/**", "/api/payment/**", "/error", "/ws/**").permitAll()

                        // 2. [QUAN TRỌNG] Chỉ cho phép GET công khai đối với các thông tin phòng
                        // Điều này giúp: Xem chi tiết, Tìm kiếm, và Lịch sử giá không cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/admin/amenities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/public-profile/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/top-landlords").permitAll()

                        // 3. Các yêu cầu còn lại (POST, PUT, DELETE) bắt buộc phải đăng nhập
                        // Ví dụ: Đăng tin mới, cập nhật giá, xóa phòng...
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    
}