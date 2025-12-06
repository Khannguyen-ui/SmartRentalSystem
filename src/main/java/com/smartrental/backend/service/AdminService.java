package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.entity.Room;
import java.util.List;

public interface AdminService {

    // 1. Lấy danh sách các tin đăng đang chờ duyệt (Status = PENDING)
    List<Room> getPendingRooms();

    // 2. Duyệt tin đăng (Active) hoặc Từ chối (Hidden)
    // Có kèm logic trừ tiền ví của chủ trọ
    void approveRoom(Long roomId, ApproveRequestDTO dto);

    // 3. Duyệt hồ sơ định danh (KYC) của chủ trọ
    void approveKYC(Long userId, ApproveRequestDTO dto);

    // 4. Thống kê tổng số người dùng hệ thống
    Long getTotalUsers();
}