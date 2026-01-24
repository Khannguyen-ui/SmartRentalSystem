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
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtUtils.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

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

                // TỐI ƯU: Ép kiểu và cập nhật trực tiếp, tránh query lại database
                if (userDetails instanceof User user) {
                    updateLastActiveTimeOptimized(user);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void updateLastActiveTimeOptimized(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = user.getLastActiveAt();

        // Throttling: Chỉ lưu nếu cách nhau >= 2 phút
        if (lastActive == null || ChronoUnit.MINUTES.between(lastActive, now) >= 2) {
            user.setLastActiveAt(now);
            // Hibernate sẽ thực hiện lệnh UPDATE vì object này đã có ID (Attached/Persistent)
            userRepository.save(user);
        }
    }
}