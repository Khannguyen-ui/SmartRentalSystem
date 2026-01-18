package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.KycRequestDTO;
import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.LandlordStatsDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.UserMapper;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.UserService;
import com.smartrental.backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
        // 1. Check trùng email
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        // 2. Convert DTO -> Entity
        // (Lưu ý: Role đã được map trong UserMapper)
        User user = userMapper.toEntity(registerDTO);

        // 3. Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // 4. Set giá trị mặc định cho tài khoản mới
        user.setWalletBalance(BigDecimal.ZERO);
        user.setKycStatus("UNVERIFIED");

        // Đảm bảo các trường mới là null (tránh lỗi rác)
        user.setAvatarUrl(null);
        user.setCitizenImages(null);
        user.setLifestyleProfile(null);

        // 5. Lưu DB
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginDTO loginDTO) {
        // 1. Xác thực Username/Password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );
        } catch (Exception e) {
            // --- THÊM 2 DÒNG NÀY ĐỂ SOI LỖI ---
            e.printStackTrace(); // In lỗi chi tiết ra Terminal
            throw new RuntimeException("Lỗi đăng nhập: " + e.getMessage()); // Hiển thị lỗi cụ thể lên Postman/Web
            // ----------------------------------
        }

        // 2. Tìm User
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Sinh Token
        String token = jwtUtils.generateToken(user);

        // 4. Trả về Response
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
    @Override
    public List<LandlordStatsDTO> getTopLandlords(double lat, double lng, double radius) {
        // Gọi Native Query lấy dữ liệu thô
        List<Object[]> results = userRepository.findTopLandlordsNearbyRaw(lat, lng, radius, PageRequest.of(0, 5));

        // Map từ Object[] sang DTO
        return results.stream()
                .map(row -> new LandlordStatsDTO(
                        ((Number) row[0]).longValue(),  // id
                        (String) row[1],               // fullName
                        (String) row[2],               // avatarUrl
                        ((Number) row[3]).longValue()  // postCount
                ))
                .collect(Collectors.toList());
    }
    @Override
    @Transactional // Nhớ thêm @Transactional để đảm bảo lưu DB an toàn
    public void upgradeToLandlord() {
        // 1. Lấy user hiện tại từ Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Kiểm tra nếu đã là chủ trọ rồi thì thôi
        if (user.getRole() == User.Role.LANDLORD) {
            throw new RuntimeException("Bạn đã là Chủ trọ rồi!");
        }

        // 3. Cập nhật Role
        user.setRole(User.Role.LANDLORD);

        // (Tùy chọn) Reset lại trạng thái KYC về UNVERIFIED để bắt xác minh lại danh tính
        // user.setKycStatus("UNVERIFIED");

        userRepository.save(user);
    }
    @Override
    @Transactional
    public void submitKyc(KycRequestDTO dto) {
        // 1. Lấy user hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate
        if ("VERIFIED".equals(user.getKycStatus())) {
            throw new RuntimeException("Tài khoản đã được xác minh danh tính rồi!");
        }

        // 3. Cập nhật thông tin KYC
        user.setCitizenId(dto.getCitizenId());
        user.setCitizenImages(dto.getCitizenImages()); // Lưu list ảnh
        user.setKycStatus("PENDING"); // Chuyển trạng thái chờ duyệt

        userRepository.save(user);
    }



}