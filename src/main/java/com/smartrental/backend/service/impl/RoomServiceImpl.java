package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.RoomMapper;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final ServicePackageRepository servicePackageRepository;

    // Cấu hình Geometry để lưu tọa độ
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Hàm tiện ích lấy user đang đăng nhập
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public RoomResponseDTO createRoom(RoomCreateDTO dto) {
        User landlord = getCurrentUser();
        if (landlord.getRole() != User.Role.LANDLORD) {
            throw new RuntimeException("Chỉ chủ trọ mới được đăng tin!");
        }

        // 2. Lấy thông tin gói cước để biết số ngày được phép hiển thị
        var servicePackage = servicePackageRepository.findById(dto.getServicePackageId())
                .orElseThrow(() -> new RuntimeException("Gói dịch vụ không tồn tại"));

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        Room room = Room.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .deposit(dto.getDeposit())
                .area(dto.getArea())
                .address(dto.getAddress())
                .servicePackageId(dto.getServicePackageId())
                .packageType(servicePackage.getName()) // Lưu: "Hội viên Vàng", "VIP",...
                .priorityLevel(servicePackage.getPriorityLevel())
                .rentalType(dto.getRentalType())
                .capacity(dto.getCapacity())
                .furnitureStatus(dto.getFurnitureStatus())
                .legalStatus(dto.getLegalStatus())
                .direction(dto.getDirection())
                .floorNumber(dto.getFloorNumber())
                .numBedrooms(dto.getNumBedrooms())
                .numBathrooms(dto.getNumBathrooms())
                .genderConstraint(dto.getGenderConstraint())
                .currentTenants(0)
                .location(point)
                .images(dto.getImages())
                .amenities(dto.getAmenities())
                .videoUrl(dto.getVideoUrl())
                .status(Room.Status.PENDING)

                // Cập nhật: Ngày hết hạn = Hôm nay + số ngày của gói cước
                .expirationDate(LocalDateTime.now().plusDays(servicePackage.getDurationDays()))

                .landlord(landlord)
                .build();

        return roomMapper.toResponse(roomRepository.save(room));
    }

    // --- LOGIC CẬP NHẬT PHÒNG (MỚI) ---
    @Override
    @Transactional
    public RoomResponseDTO updateRoom(Long id, RoomUpdateDTO dto) {
        // 1. Tìm phòng
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. Xác định người dùng và quyền hạn
        User currentUser = getCurrentUser();
        boolean isOwner = room.getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        // Kiểm tra quyền sở hữu
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa phòng này");
        }

        // 3. LOGIC MỚI: Chỉ cho phép sửa khi chưa được duyệt (PENDING hoặc REJECTED)
        // Nếu là Admin thì vẫn cho sửa thoải mái (tùy bạn quyết định, ở đây mình để Admin toàn quyền)
        if (!isAdmin && room.getStatus() == Room.Status.APPROVED) {
            throw new RuntimeException("Tin đăng đã được duyệt và đang hiển thị. Bạn không thể chỉnh sửa! Hãy ẩn tin hoặc xóa đi đăng lại.");
        }

        // 4. Cập nhật thông tin (Phần còn lại giữ nguyên)
        if (dto.getTitle() != null) room.setTitle(dto.getTitle());
        if (dto.getDescription() != null) room.setDescription(dto.getDescription());
        if (dto.getPrice() != null) room.setPrice(dto.getPrice());
        if (dto.getDeposit() != null) room.setDeposit(dto.getDeposit());
        if (dto.getArea() != null) room.setArea(dto.getArea());
        if (dto.getAddress() != null) room.setAddress(dto.getAddress());
        if (dto.getRentalType() != null) room.setRentalType(dto.getRentalType());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getFurnitureStatus() != null) room.setFurnitureStatus(dto.getFurnitureStatus());
        if (dto.getLegalStatus() != null) room.setLegalStatus(dto.getLegalStatus());
        if (dto.getDirection() != null) room.setDirection(dto.getDirection());
        if (dto.getFloorNumber() != null) room.setFloorNumber(dto.getFloorNumber());
        if (dto.getNumBedrooms() != null) room.setNumBedrooms(dto.getNumBedrooms());
        if (dto.getNumBathrooms() != null) room.setNumBathrooms(dto.getNumBathrooms());
        if (dto.getGenderConstraint() != null) room.setGenderConstraint(dto.getGenderConstraint());
        if (dto.getVideoUrl() != null) room.setVideoUrl(dto.getVideoUrl());

        if (dto.getAmenities() != null) {
            room.setAmenities(dto.getAmenities());
        }

        if (dto.getImages() != null) {
            room.setImages(dto.getImages());
        }

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
            room.setLocation(point);
        }

        // Nếu phòng đang bị TỪ CHỐI (REJECTED), khi sửa xong thì chuyển lại thành PENDING để Admin duyệt lại
        if (room.getStatus() == Room.Status.REJECTED) {
            room.setStatus(Room.Status.PENDING);
        }

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public Page<RoomResponseDTO> searchNearby(double lat, double lng, double radius, String keyword, Pageable pageable) {
        // 1. Gọi Repository kèm pageable
        Page<Room> rooms = roomRepository.findRoomsNearby(lat, lng, radius, keyword, pageable);

        // 2. Sử dụng method map của Page để chuyển đổi Entity sang DTO
        return rooms.map(roomMapper::toResponse);
    }

    @Override
    public List<RoomResponseDTO> getMyRooms() {
        User landlord = getCurrentUser();
        return roomRepository.findByLandlordId(landlord.getId())
                .stream().map(roomMapper::toResponse).collect(Collectors.toList());
    }

    // --- LOGIC XÓA PHÒNG (CẬP NHẬT) ---
    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        User currentUser = getCurrentUser();

        // Cho phép ADMIN xóa hoặc Chủ trọ sở hữu phòng xóa
        boolean isOwner = room.getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xóa phòng này");
        }

        roomRepository.delete(room);
    }

    @Override
    public RoomResponseDTO getRoomDetail(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        return roomMapper.toResponse(room);
    }
    @Override
    public List<RoomResponseDTO> getRoomsByLandlord(Long landlordId) {
        // 1. Lấy tất cả phòng của chủ trọ từ DB
        List<Room> rooms = roomRepository.findByLandlordId(landlordId);

        // 2. Lọc và Convert sang DTO
        return rooms.stream()
                .filter(r -> r.getStatus() == Room.Status.ACTIVE || r.getStatus() == Room.Status.FULL) // Chỉ lấy Active hoặc Full
                .map(roomMapper::toResponse)
                // Sắp xếp: ACTIVE lên trước, sau đó đến mới nhất
                .sorted((r1, r2) -> {
                    if (r1.getStatus().equals(r2.getStatus())) {
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    }
                    return "ACTIVE".equals(r1.getStatus()) ? -1 : 1;
                })
                .collect(Collectors.toList());
    }
    @Override
    public Page<RoomResponseDTO> getRoomsWithVideo(Pageable pageable) {
        return roomRepository.findAllWithVideo(pageable).map(roomMapper::toResponse);
    }

    // =====================================================================
    //          CÁC HÀM MỚI (ĐÃ SỬA LẠI GET/SET WALLET BALANCE)
    // =====================================================================

    @Override
    @Transactional
    public void pushRoomToTop(Long roomId, Long packageId) {
        // 1. Tìm Phòng & Gói cước
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại"));

        User landlord = room.getLandlord();

        // 2. Kiểm tra số dư ví (Sửa getAccountBalance -> getWalletBalance)
        if (landlord.getWalletBalance() == null || landlord.getWalletBalance().compareTo(pkg.getPrice()) < 0) {
            throw new RuntimeException("Số dư tài khoản không đủ (" + pkg.getPrice() + " đ). Vui lòng nạp thêm!");
        }

        // 3. Trừ tiền (Sửa setAccountBalance -> setWalletBalance)
        landlord.setWalletBalance(landlord.getWalletBalance().subtract(pkg.getPrice()));
        userRepository.save(landlord);

        // 4. Cập nhật thông tin Phòng
        room.setServicePackageId(pkg.getId()); // Cập nhật ID gói
        room.setPriorityLevel(pkg.getPriorityLevel()); // Cập nhật độ ưu tiên hiển thị
        room.setPackageType(pkg.getName()); // Lưu tên gói

        // Reset thời hạn tin = Thời điểm hiện tại + Số ngày của gói
        // Logic cộng dồn (Khuyên dùng)
        if (room.getStatus() == Room.Status.ACTIVE &&
                room.getExpirationDate() != null &&
                room.getExpirationDate().isAfter(LocalDateTime.now())) {
            // Nếu còn hạn thì cộng tiếp vào đuôi
            room.setExpirationDate(room.getExpirationDate().plusDays(pkg.getDurationDays()));
        } else {
            // Nếu hết hạn thì tính từ bây giờ
            room.setExpirationDate(LocalDateTime.now().plusDays(pkg.getDurationDays()));
        }
        room.setLastPushedAt(LocalDateTime.now());

        // Nếu tin đang bị Ẩn hoặc Hết hạn -> Chuyển sang ACTIVE ngay
        if (room.getStatus() == Room.Status.HIDDEN || room.getStatus() == Room.Status.EXPIRED) {
            room.setStatus(Room.Status.ACTIVE);
        }

        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void toggleAutoRenew(Long roomId, boolean enable) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        User currentUser = getCurrentUser();
        if (!room.getLandlord().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền cài đặt phòng này");
        }

        room.setAutoRenew(enable);
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void updateRoomStatus(Long roomId, String statusStr) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        User currentUser = getCurrentUser();
        if (!room.getLandlord().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Không có quyền");
        }

        try {
            Room.Status newStatus = Room.Status.valueOf(statusStr.toUpperCase());

            // Logic chặn: Không cho chuyển sang ACTIVE nếu đã hết hạn
            if (newStatus == Room.Status.ACTIVE &&
                    (room.getExpirationDate() == null || room.getExpirationDate().isBefore(LocalDateTime.now()))) {
                throw new RuntimeException("Tin đã hết hạn, vui lòng Đẩy tin (Gia hạn) trước khi kích hoạt lại.");
            }

            room.setStatus(newStatus);
            roomRepository.save(room);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
        }
    }
}