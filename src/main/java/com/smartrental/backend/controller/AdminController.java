package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.service.impl.AdminServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminServiceImpl adminService;

    // API DUYỆT TIN ĐĂNG
    // Method: PUT
    // URL: /api/admin/rooms/{id}/approve
    @PutMapping("/rooms/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được gọi
    public ResponseEntity<?> approveRoom(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        adminService.approveRoom(id, dto);
        return ResponseEntity.ok("Đã cập nhật trạng thái phòng!");
    }

    // API DUYỆT KYC NGƯỜI DÙNG
    @PutMapping("/users/{id}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveKYC(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        adminService.approveKYC(id, dto);
        return ResponseEntity.ok("Đã xử lý hồ sơ KYC!");
    }

    // API THỐNG KÊ
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok("Total Users: " + adminService.getTotalUsers());
    }
}