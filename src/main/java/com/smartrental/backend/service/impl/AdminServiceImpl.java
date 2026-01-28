package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO; // Import DTO
import com.smartrental.backend.entity.*;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.AdminService;
import com.smartrental.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartrental.backend.mapper.RoomMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository packageRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomMapper roomMapper;
    private final NotificationService notificationService;

    @Override
    public List<RoomResponseDTO> getPendingRooms() {
        // 1. Lấy dữ liệu thô từ DB
        List<Room> rooms = roomRepository.findByStatus(Room.Status.PENDING);

        // 2. Dùng Mapper chuyển sang DTO (Tách Point thành lat/lng)
        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveRoom(Long roomId, ApproveRequestDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (room.getStatus() == Room.Status.ACTIVE) {
            throw new RuntimeException("Lỗi: Phòng này đã hoạt động!");
        }

        User landlord = userRepository.findById(room.getLandlord().getId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        if (dto.isApproved()) {
            ServicePackage servicePackage = packageRepository.findById(room.getServicePackageId())
                    .orElseThrow(() -> new RuntimeException("Gói cước không hợp lệ"));

            // LOGIC NÂNG CAO: Kiểm tra giảm giá nếu là Hội viên
            BigDecimal finalPrice = servicePackage.getPrice();
            if (landlord.getMembershipPackage() != null) {
                double discount = landlord.getMembershipPackage().getDiscountPercent() / 100.0;
                BigDecimal discountAmount = finalPrice.multiply(BigDecimal.valueOf(discount));
                finalPrice = finalPrice.subtract(discountAmount);
                System.out.println(">>> Hội viên được giảm giá: " + discountAmount);
            }

            if (landlord.getWalletBalance().compareTo(finalPrice) < 0) {
                throw new RuntimeException("Chủ trọ không đủ tiền (Cần: " + finalPrice + ")");
            }

            // Trừ tiền
            landlord.setWalletBalance(landlord.getWalletBalance().subtract(finalPrice));
            userRepository.save(landlord);

            // Lưu giao dịch
            Transaction transaction = transactionRepository.save(Transaction.builder()
                    .user(landlord)
                    .amount(finalPrice.negate())
                    .type("POST_FEE")
                    .status("SUCCESS")
                    .vnpayCode("INTERNAL_" + System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .build());

            // Cập nhật trạng thái phòng
            room.setStatus(Room.Status.ACTIVE);
            room.setApprovedAt(LocalDateTime.now());

            // Tính ngày hết hạn
            LocalDateTime expiryBase = (room.getExpirationDate() != null && room.getExpirationDate().isAfter(LocalDateTime.now()))
                    ? room.getExpirationDate() : LocalDateTime.now();
            room.setExpirationDate(expiryBase.plusDays(servicePackage.getDurationDays()));

            // 2. GỬI THÔNG BÁO CHO CHỦ TRỌ
            notificationService.sendNotification(
                    landlord,
                    "Tin đăng đã được duyệt",
                    "Phòng '" + room.getTitle() + "' của bạn đã được phê duyệt và hiển thị trên hệ thống.",
                    NotificationType.SYSTEM,
                    room.getId()
            );

        } else {
            room.setStatus(Room.Status.REJECTED);
            // Gửi thông báo từ chối
            notificationService.sendNotification(
                    landlord,
                    "Tin đăng bị từ chối",
                    "Rất tiếc, tin đăng '" + room.getTitle() + "' không được duyệt. Lý do: " + dto.getReason(),
                    NotificationType.SYSTEM,
                    room.getId()
            );
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
            // Bổ sung thông báo thành công
            notificationService.sendNotification(
                    user, "Xác minh danh tính thành công",
                    "Hồ sơ định danh của bạn đã được phê duyệt. Tài khoản đã có dấu tích xanh uy tín!",
                    NotificationType.KYC_STATUS, user.getId()
            );
        } else {
            user.setKycStatus("REJECTED");
            // Bổ sung thông báo từ chối kèm lý do
            notificationService.sendNotification(
                    user, "Xác minh danh tính thất bại",
                    "Rất tiếc, hồ sơ của bạn không được phê duyệt. Lý do: " + dto.getReason(),
                    NotificationType.KYC_STATUS, user.getId()
            );
        }
        userRepository.save(user);
    }

    @Override
    public Long getTotalUsers() {
        return userRepository.count();
    }

    // --- CÁC HÀM QUẢN LÝ USER ---

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User createUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .role(User.Role.valueOf(dto.getRole().toUpperCase()))
                .walletBalance(BigDecimal.ZERO)
                .kycStatus("UNVERIFIED")
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserRegisterDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (dto.getFullName() != null && !dto.getFullName().isEmpty())
            user.setFullName(dto.getFullName());

        if (dto.getPhone() != null && !dto.getPhone().isEmpty())
            user.setPhone(dto.getPhone());

        if (dto.getRole() != null && !dto.getRole().isEmpty())
            user.setRole(User.Role.valueOf(dto.getRole().toUpperCase()));

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User không tồn tại");
        }
        userRepository.deleteById(id);
    }
}