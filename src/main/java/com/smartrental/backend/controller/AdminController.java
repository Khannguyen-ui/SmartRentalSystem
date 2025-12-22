package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO; // <--- QUAN TRỌNG: Import DTO
import com.smartrental.backend.entity.User;
import com.smartrental.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // --- API DUYỆT CŨ ---
    @PutMapping("/rooms/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRoom(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        adminService.approveRoom(id, dto);
        return ResponseEntity.ok("Đã cập nhật trạng thái phòng!");
    }

    @PutMapping("/users/{id}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveKYC(@PathVariable Long id, @RequestBody ApproveRequestDTO dto) {
        adminService.approveKYC(id, dto);
        return ResponseEntity.ok("Đã xử lý hồ sơ KYC!");
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok("Total Users: " + adminService.getTotalUsers());
    }

    // --- API QUẢN LÝ USER MỚI ---

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
        return ResponseEntity.ok("Đã thay đổi trạng thái hoạt động của tài khoản!");
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("Đã xóa tài khoản vĩnh viễn!");
    }

    // --- SỬA LẠI ĐOẠN NÀY ĐỂ KHỚP VỚI SERVICE ---
    // Cũ (Sai): public ResponseEntity<List<Room>> getPendingRooms()
    // Mới (Đúng): Trả về DTO để tránh lỗi "envelope"
    @GetMapping("/rooms/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomResponseDTO>> getPendingRooms() {
        return ResponseEntity.ok(adminService.getPendingRooms());
    }
}