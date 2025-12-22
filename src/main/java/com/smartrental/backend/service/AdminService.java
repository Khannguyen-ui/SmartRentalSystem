package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.dto.response.RoomResponseDTO; // Import DTO

import java.util.List;

public interface AdminService {

    // 1. Lấy danh sách các tin đăng đang chờ duyệt (Status = PENDING)
    List<RoomResponseDTO> getPendingRooms(); // <--- Phải trả về DTO

    void approveRoom(Long roomId, ApproveRequestDTO dto);
    void approveKYC(Long userId, ApproveRequestDTO dto);
    Long getTotalUsers();

    List<User> getAllUsers();
    User createUser(UserRegisterDTO dto);
    User updateUser(Long id, UserRegisterDTO dto);
    void deleteUser(Long id);
    void toggleUserStatus(Long id);
}