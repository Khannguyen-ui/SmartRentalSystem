package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ForgotPasswordRequest;
import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.ResetPasswordRequest;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 1. API Đăng ký
    // URL: http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        UserResponseDTO newUser = userService.register(registerDTO);
        return ResponseEntity.ok(newUser);
    }

    // 2. API Đăng nhập
    // URL: http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginDTO loginDTO) {
        AuthResponse response = userService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.sendForgotPasswordEmail(request.getEmail());
        return ResponseEntity.ok(java.util.Map.of("message", "Link đã được gửi tới email của bạn"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Mật khẩu đã được cập nhật"));
    }
}