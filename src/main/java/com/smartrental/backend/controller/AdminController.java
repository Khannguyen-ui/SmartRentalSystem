package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import thêm Map

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==========================================
    // 1. QUẢN LÝ TIN ĐĂNG & KYC (QUAN TRỌNG)
    // ==========================================

    @PutMapping("/rooms/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRoom(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        try {
            adminService.approveRoom(id, dto);
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái phòng!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- API DUYỆT KYC (ĐÃ SỬA ĐỂ BẮT LỖI) ---
    @PutMapping("/users/{id}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveKYC(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        try {
            // Gọi xuống Service
            adminService.approveKYC(id, dto);

            // Trả về JSON chuẩn
            return ResponseEntity.ok(Map.of("message", "Đã xử lý hồ sơ KYC thành công!"));
        } catch (Exception e) {
            // --- IN LỖI RA TERMINAL ĐỂ DEBUG ---
            System.err.println(">>> LỖI DUYỆT KYC: " + e.getMessage());
            e.printStackTrace();
            // ----------------------------------
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    // ==========================================
    // 2. QUẢN LÝ NGƯỜI DÙNG (USER CRUD)
    // ==========================================

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(adminService.createUser(dto));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(adminService.updateUser(id, dto));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(Map.of("message", "Đã thay đổi trạng thái tài khoản!"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa tài khoản vĩnh viễn!"));
    }

    // ==========================================
    // 3. THỐNG KÊ & KHÁC
    // ==========================================

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of("totalUsers", adminService.getTotalUsers()));
    }

    @GetMapping("/rooms/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomResponseDTO>> getPendingRooms() {
        return ResponseEntity.ok(adminService.getPendingRooms());
    }
}