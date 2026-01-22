package com.smartrental.backend.config;

import com.smartrental.backend.entity.User; // MỚI
import com.smartrental.backend.repository.UserRepository; // MỚI
import com.smartrental.backend.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime; // MỚI
import java.time.temporal.ChronoUnit; // MỚI
import java.util.Optional; // MỚI

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // 1. Inject thêm Repository để cập nhật giờ (Lombok sẽ tự tạo Constructor)
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // --- ĐOẠN CODE CŨ CỦA BẠN (GIỮ NGUYÊN) ---
        try {
            userEmail = jwtUtils.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }
        // ----------------------------------------

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtUtils.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 2. [MỚI] GỌI HÀM CẬP NHẬT TRẠNG THÁI HOẠT ĐỘNG
                updateLastActiveTime(userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 3. [MỚI] Hàm cập nhật thời gian active cuối cùng.
     * Cơ chế Throttling: Chỉ update DB nếu lần cuối cách đây > 2 phút
     * để tránh spam database làm chậm app.
     */
    private void updateLastActiveTime(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime lastActive = user.getLastActiveAt();

                // Nếu chưa từng active HOẶC lần active cuối > 2 phút trước thì mới lưu
                if (lastActive == null || ChronoUnit.MINUTES.between(lastActive, now) >= 2) {
                    user.setLastActiveAt(now);
                    userRepository.save(user);
                }
            }
        } catch (Exception e) {
            // Log lỗi nhẹ (console) nhưng không được làm chết request của user
            System.err.println("Lỗi cập nhật LastActive (không ảnh hưởng luồng chính): " + e.getMessage());
        }
    }
}