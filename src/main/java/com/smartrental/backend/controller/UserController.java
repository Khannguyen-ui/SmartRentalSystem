package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.UserProfileDTO;
import com.smartrental.backend.dto.response.LandlordStatsDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.UserMapper;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.UserService;
import com.smartrental.backend.service.impl.FptAiService; // 1. Import Service FPT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType; // 2. Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 3. Import MultipartFile
import com.smartrental.backend.dto.request.KycRequestDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    // 4. Inject FptAiService (Lombok sẽ tự tạo Constructor vì có final)
    private final FptAiService fptAiService;

    // --- 1. Lấy thông tin cá nhân (Profile) ---
    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user);
    }

    // --- 2. Cập nhật hồ sơ & Lối sống ---
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody UserProfileDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Cập nhật các trường
        if(dto.getFullName() != null) user.setFullName(dto.getFullName());
        if(dto.getPhone() != null) user.setPhone(dto.getPhone());
        if(dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if(dto.getCitizenId() != null) user.setCitizenId(dto.getCitizenId());

        // Cập nhật Lối sống (JSON)
        if(dto.getLifestyleProfile() != null) {
            user.setLifestyleProfile(dto.getLifestyleProfile());
        }

        return ResponseEntity.ok(userMapper.toResponse(userRepository.save(user)));
    }

    // --- 3. Lấy Top chủ trọ ---
    @GetMapping("/top-landlords")
    public ResponseEntity<List<LandlordStatsDTO>> getTopLandlords(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10000") double radius
    ) {
        return ResponseEntity.ok(userService.getTopLandlords(lat, lng, radius));
    }

    // --- 4. Nâng cấp lên Chủ trọ ---
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeToLandlord() {
        userService.upgradeToLandlord();
        return ResponseEntity.ok(Map.of("message", "Nâng cấp thành công! Vui lòng đăng nhập lại."));
    }

    // --- 5. API E-KYC (OCR - Trích xuất CCCD bằng FPT.AI) ---
    // Frontend gửi form-data: key="file", value=[File Ảnh]
    @PostMapping(value = "/extract-id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractIdCardInfo(@RequestParam("file") MultipartFile file) {
        try {
            // Gọi Service FPT.AI để xử lý
            Map<String, String> info = fptAiService.scanIdCard(file);

            // Trả về JSON: { "citizenId": "001...", "fullName": "NGUYEN VAN A" }
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi đọc ảnh: " + e.getMessage()));
        }
    }
    @PostMapping("/kyc")
    public ResponseEntity<?> submitKyc(@RequestBody @Valid KycRequestDTO dto) {
        userService.submitKyc(dto); // Hàm này phải có trong UserServiceImpl (đã làm ở các bước trước)
        return ResponseEntity.ok(Map.of("message", "Gửi yêu cầu xác minh thành công!"));
    }

}