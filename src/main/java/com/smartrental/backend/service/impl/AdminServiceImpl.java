package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository packageRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<Room> getPendingRooms() {
        return roomRepository.findByStatus(Room.Status.PENDING);
    }

    @Override
    @Transactional
    public void approveRoom(Long roomId, ApproveRequestDTO dto) {
        // 1. Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // --- LOGIC MỚI: CHẶN DUYỆT LẠI ---
        if (room.getStatus() == Room.Status.ACTIVE) {
            // Ném ra lỗi để báo cho Admin biết, dừng mọi việc lại ngay lập tức
            throw new RuntimeException("Lỗi: Phòng này ĐÃ ĐƯỢC DUYỆT và đang hoạt động rồi!");
        }
        // ---------------------------------

        User landlord = userRepository.findById(room.getLandlord().getId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        if (dto.isApproved()) {
            // --- LOGIC TRỪ TIỀN ---

            if (room.getServicePackageId() == null) {
                throw new RuntimeException("Phòng này chưa chọn gói dịch vụ (ID null)!");
            }

            ServicePackage servicePackage = packageRepository.findById(room.getServicePackageId())
                    .orElseThrow(() -> new RuntimeException("Gói cước không hợp lệ"));

            if (landlord.getWalletBalance().compareTo(servicePackage.getPrice()) < 0) {
                throw new RuntimeException("Chủ trọ không đủ tiền (" + servicePackage.getPrice() + " VND)!");
            }

            // Trừ tiền
            landlord.setWalletBalance(landlord.getWalletBalance().subtract(servicePackage.getPrice()));
            userRepository.save(landlord);

            // Lưu giao dịch
            Transaction transaction = Transaction.builder()
                    .user(landlord)
                    .amount(servicePackage.getPrice().negate())
                    .type("POST_FEE")
                    .status("SUCCESS")
                    .vnpayCode("INTERNAL_" + System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);

            // Active phòng
            room.setStatus(Room.Status.ACTIVE);

            // Tính ngày hết hạn
            LocalDateTime expiryBase = (room.getExpirationDate() != null && room.getExpirationDate().isAfter(LocalDateTime.now()))
                    ? room.getExpirationDate()
                    : LocalDateTime.now();
            room.setExpirationDate(expiryBase.plusDays(servicePackage.getDurationDays()));

            System.out.println(">>> DUYỆT THÀNH CÔNG! ĐÃ TRỪ TIỀN.");

        } else {
            // Từ chối -> Ẩn
            room.setStatus(Room.Status.HIDDEN);
        }
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void approveKYC(Long userId, ApproveRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (dto.isApproved()) {
            user.setKycStatus("VERIFIED");
        } else {
            user.setKycStatus("REJECTED");
        }
        userRepository.save(user);
    }

    @Override
    public Long getTotalUsers() {
        return userRepository.count();
    }
}