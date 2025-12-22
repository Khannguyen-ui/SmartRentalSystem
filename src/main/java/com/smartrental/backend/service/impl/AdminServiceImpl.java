package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ApproveRequestDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO; // Import DTO
import com.smartrental.backend.entity.*;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.AdminService;
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
            throw new RuntimeException("Lỗi: Phòng này ĐÃ ĐƯỢC DUYỆT và đang hoạt động rồi!");
        }

        User landlord = userRepository.findById(room.getLandlord().getId())
                .orElseThrow(() -> new RuntimeException("Chủ trọ không tồn tại"));

        if (dto.isApproved()) {
            if (room.getServicePackageId() == null) {
                throw new RuntimeException("Phòng này chưa chọn gói dịch vụ (ID null)!");
            }
            ServicePackage servicePackage = packageRepository.findById(room.getServicePackageId())
                    .orElseThrow(() -> new RuntimeException("Gói cước không hợp lệ"));

            if (landlord.getWalletBalance().compareTo(servicePackage.getPrice()) < 0) {
                throw new RuntimeException("Chủ trọ không đủ tiền!");
            }

            landlord.setWalletBalance(landlord.getWalletBalance().subtract(servicePackage.getPrice()));
            userRepository.save(landlord);

            Transaction transaction = Transaction.builder()
                    .user(landlord)
                    .amount(servicePackage.getPrice().negate())
                    .type("POST_FEE")
                    .status("SUCCESS")
                    .vnpayCode("INTERNAL_" + System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);

            room.setStatus(Room.Status.ACTIVE);
            LocalDateTime expiryBase = (room.getExpirationDate() != null && room.getExpirationDate().isAfter(LocalDateTime.now()))
                    ? room.getExpirationDate()
                    : LocalDateTime.now();
            room.setExpirationDate(expiryBase.plusDays(servicePackage.getDurationDays()));
            System.out.println(">>> DUYỆT THÀNH CÔNG! ĐÃ TRỪ TIỀN.");
        } else {
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