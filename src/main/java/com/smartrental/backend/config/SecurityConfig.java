package com.smartrental.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
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
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Kích hoạt CORS (Nó sẽ tự tìm đến file CorsConfig của bạn)
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 2. Các API công khai hoàn toàn
                        .requestMatchers("/api/auth/**", "/api/payment/**","/", "/error", "/ws/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        // 3. Cho phép xem công khai (Review và Room)
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/room/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/favorites/check/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/admin/amenities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/public-profile/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/top-landlords").permitAll()

                        // 4. Các yêu cầu còn lại (Lưu tin, Gửi bình luận) phải login
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