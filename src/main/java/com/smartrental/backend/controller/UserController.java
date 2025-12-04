package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.UserProfileDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.UserMapper;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // 1. Lấy thông tin cá nhân (Profile)
    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user);
    }

    // 2. Cập nhật hồ sơ & Lối sống
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
}