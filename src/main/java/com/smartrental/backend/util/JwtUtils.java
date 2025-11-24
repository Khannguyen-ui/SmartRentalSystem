package com.smartrental.backend.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 1. Trích xuất Username (Email) từ Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Trích xuất một thông tin cụ thể (Claim)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Tạo Token (Không có claims phụ)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // 4. Tạo Token (Có claims phụ - ví dụ muốn lưu thêm role vào token)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Lưu email vào Subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. Kiểm tra Token có hợp lệ không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 6. Kiểm tra Token hết hạn chưa
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 7. Giải mã toàn bộ thông tin trong Token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 8. Lấy Key ký tên (Quan trọng)
    private Key getSignInKey() {
        // Vì trong application.properties bạn để chuỗi văn bản thường (KhanNguyen...)
        // Nên ta dùng getBytes() trực tiếp.
        return Keys.hmacShaKeyFor(secretKey.getBytes());
        
        // LƯU Ý: Nếu sau này bạn dùng chuỗi Base64 chuẩn, hãy đổi thành dòng dưới:
        // byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // return Keys.hmacShaKeyFor(keyBytes);
    }
}