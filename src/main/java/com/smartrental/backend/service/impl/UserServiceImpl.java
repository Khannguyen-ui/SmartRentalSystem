package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.KycRequestDTO;
import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.LandlordPublicProfileDTO;
import com.smartrental.backend.dto.response.LandlordStatsDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.UserMapper;

// --- 1. IMPORT CÁC REPOSITORY MỚI ---
import com.smartrental.backend.repository.ContractRepository;
import com.smartrental.backend.repository.ReviewRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;

import com.smartrental.backend.service.UserService;
import com.smartrental.backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // --- 2. KHAI BÁO (INJECT) CÁC REPOSITORY CÒN THIẾU ---
    // (Bắt buộc phải có các dòng này thì mới dùng được ở dưới)
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final ReviewRepository reviewRepository;


    @Override
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        User user = userMapper.toEntity(registerDTO);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setWalletBalance(BigDecimal.ZERO);
        user.setKycStatus("UNVERIFIED");

        // Reset null để tránh lỗi rác
        user.setAvatarUrl(null);
        user.setCitizenImages(null);
        user.setLifestyleProfile(null);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi đăng nhập: " + e.getMessage());
        }

        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtils.generateToken(user);

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
        List<Object[]> results = userRepository.findTopLandlordsNearbyRaw(lat, lng, radius, PageRequest.of(0, 5));

        return results.stream()
                .map(row -> new LandlordStatsDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void upgradeToLandlord() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == User.Role.LANDLORD) {
            throw new RuntimeException("Bạn đã là Chủ trọ rồi!");
        }

        user.setRole(User.Role.LANDLORD);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void submitKyc(KycRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("VERIFIED".equals(user.getKycStatus())) {
            throw new RuntimeException("Tài khoản đã được xác minh danh tính rồi!");
        }

        user.setCitizenId(dto.getCitizenId());
        user.setCitizenImages(dto.getCitizenImages());
        user.setKycStatus("PENDING");

        userRepository.save(user);
    }

    // --- 3. HÀM LẤY PUBLIC PROFILE (ĐÃ FIX LỖI IMPORT) ---
    @Override
    @Transactional(readOnly = true)
    public LandlordPublicProfileDTO getLandlordPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ trọ"));

        // Gọi Repository lấy số liệu (Giờ đã có biến để gọi)
        int totalRooms = roomRepository.countByLandlordId(id);
        int successfulDeals = contractRepository.countSuccessfulDealsByLandlord(id);
        Double avgRating = reviewRepository.getAverageRatingByLandlord(id);
        int totalReviews = reviewRepository.countReviewsByLandlord(id);

        // Xử lý logic cắt chuỗi địa chỉ
        List<String> rawAddresses = roomRepository.findAddressesByLandlordId(id);
        List<String> activeDistricts = extractDistrictsFromAddresses(rawAddresses);

        // Đóng gói DTO
        return LandlordPublicProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .joinDate(user.getCreatedAt())
                .lastActiveAt(user.getLastActiveAt())
                .totalRooms(totalRooms)
                .successfulDeals(successfulDeals)
                .totalReviews(totalReviews)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .activeDistricts(activeDistricts)
                .build();
    }

    // --- Private Helper Method ---
    private List<String> extractDistrictsFromAddresses(List<String> addresses) {
        Set<String> districtSet = new HashSet<>();
        // Regex biên dịch sẵn
        Pattern pattern = Pattern.compile("(?i)(Quận|Huyện|Thị xã|TP|Thành phố)\\s+([\\p{L}0-9\\s]+)");

        for (String addr : addresses) {
            if (addr == null) continue;
            String[] parts = addr.split(",");
            boolean found = false;

            for (String part : parts) {
                String trimPart = part.trim();
                Matcher matcher = pattern.matcher(trimPart);
                if (matcher.find()) {
                    districtSet.add(trimPart);
                    found = true;
                    break;
                }
            }
            // Fallback: lấy phần tử áp chót
            if (!found && parts.length >= 3) {
                districtSet.add(parts[parts.length - 2].trim());
            }
        }
        return districtSet.stream().limit(3).toList();
    }
}