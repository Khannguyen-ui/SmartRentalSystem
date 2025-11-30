package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // 1. Lấy danh sách tin chờ duyệt
    public List<Room> getPendingRooms() {
        // Bạn cần thêm method findByStatus vào RoomRepository nhé
        // return roomRepository.findByStatus(Room.Status.PENDING);
        return List.of(); // Demo
    }

    // 2. Duyệt tin đăng
    @Transactional
    public void approveRoom(Long roomId, ApproveRequestDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (dto.isApproved()) {
            room.setStatus(Room.Status.ACTIVE);
        } else {
            // Nếu từ chối -> Có thể set status là REJECTED hoặc xóa luôn
            // Ở đây mình demo set HIDDEN tạm
            room.setStatus(Room.Status.HIDDEN);
            // TODO: Gửi thông báo lý do cho chủ trọ qua NotificationService
        }
        roomRepository.save(room);
    }

    // 3. Duyệt KYC (Xác thực chủ trọ)
    @Transactional
    public void approveKYC(Long userId, ApproveRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (dto.isApproved()) {
            user.setKycStatus("VERIFIED");
            // Có thể tặng tiền thưởng vào ví khi KYC thành công
        } else {
            user.setKycStatus("REJECTED");
        }
        userRepository.save(user);
    }

    // 4. Thống kê Doanh thu (Sơ bộ)
    public Long getTotalUsers() {
        return userRepository.count();
    }
}