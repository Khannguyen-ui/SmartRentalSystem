package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.RoomMapper;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.RoomService;
import com.smartrental.backend.service.NotificationService;
import jakarta.persistence.EntityManager;      // <--- MỚI
import jakarta.persistence.PersistenceContext; // <--- MỚI
import jakarta.persistence.Query;              // <--- MỚI
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // <--- MỚI
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
    private final NotificationService notificationService;
    private final ServicePackageRepository servicePackageRepository;

    // 🟢 1. Inject EntityManager để chạy query động
    @PersistenceContext
    private EntityManager entityManager;

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

        // Lấy thông tin gói cước
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
                .packageType(servicePackage.getName())
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
                .expirationDate(LocalDateTime.now().plusDays(servicePackage.getDurationDays()))
                .landlord(landlord)
                .build();

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(Long id, RoomUpdateDTO dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        User currentUser = getCurrentUser();
        boolean isOwner = room.getLandlord().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa phòng này");
        }

        if (!isAdmin && room.getStatus() == Room.Status.APPROVED) {
            throw new RuntimeException("Tin đăng đã được duyệt. Hãy ẩn tin đi rồi sửa.");
        }

        // Cập nhật các trường thông tin nếu có gửi lên
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

        if (dto.getAmenities() != null) room.setAmenities(dto.getAmenities());
        if (dto.getImages() != null) room.setImages(dto.getImages());

        // CHỈ CẬP NHẬT TỌA ĐỘ KHI CÓ CẢ LAT VÀ LNG
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
            room.setLocation(point);
        }

        if (room.getStatus() == Room.Status.REJECTED) {
            room.setStatus(Room.Status.PENDING);
        }

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public Page<RoomResponseDTO> searchNearby(double lat, double lng, double radius, String keyword, Pageable pageable) {
        return roomRepository.findRoomsNearby(lat, lng, radius, keyword, pageable).map(roomMapper::toResponse);
    }

    @Override
    public List<RoomResponseDTO> getMyRooms() {
        User landlord = getCurrentUser();
        return roomRepository.findByLandlordId(landlord.getId())
                .stream().map(roomMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        User currentUser = getCurrentUser();
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
        List<Room> rooms = roomRepository.findByLandlordId(landlordId);
        return rooms.stream()
                .filter(r -> r.getStatus() == Room.Status.ACTIVE || r.getStatus() == Room.Status.FULL)
                .map(roomMapper::toResponse)
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

    @Override
    @Transactional
    public void pushRoomToTop(Long roomId, Long packageId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại"));

        User landlord = room.getLandlord();

        if (landlord.getWalletBalance() == null || landlord.getWalletBalance().compareTo(pkg.getPrice()) < 0) {
            throw new RuntimeException("Số dư tài khoản không đủ (" + pkg.getPrice() + " đ). Vui lòng nạp thêm!");
        }

        landlord.setWalletBalance(landlord.getWalletBalance().subtract(pkg.getPrice()));
        userRepository.save(landlord);

        room.setServicePackageId(pkg.getId());
        room.setPriorityLevel(pkg.getPriorityLevel());
        room.setPackageType(pkg.getName());

        if (room.getStatus() == Room.Status.ACTIVE &&
                room.getExpirationDate() != null &&
                room.getExpirationDate().isAfter(LocalDateTime.now())) {
            room.setExpirationDate(room.getExpirationDate().plusDays(pkg.getDurationDays()));
        } else {
            room.setExpirationDate(LocalDateTime.now().plusDays(pkg.getDurationDays()));
        }
        room.setLastPushedAt(LocalDateTime.now());

        if (room.getStatus() == Room.Status.HIDDEN || room.getStatus() == Room.Status.EXPIRED) {
            room.setStatus(Room.Status.ACTIVE);
        }
        roomRepository.save(room);
        notificationService.sendNotification(
                landlord, "Đẩy tin thành công",
                String.format("Tin đăng '%s' của bạn đã được đẩy lên đầu trang và gia hạn thêm %d ngày.",
                        room.getTitle(), pkg.getDurationDays()),
                NotificationType.ROOM_PUSH_SUCCESS, room.getId()
        );
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

    // 🟢 2. CẬP NHẬT LOGIC TÌM KIẾM NÂNG CAO TRỰC TIẾP TẠI ĐÂY
    @Override
    public Page<RoomResponseDTO> searchNearbyAdvanced(
            Double lat, Double lng, Double radius,
            String keyword, String type,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double minArea, Double maxArea,
            List<Integer> bedroomList, List<Integer> bathroomList,
            List<String> directionList, String furniture,
            Pageable pageable) {

        // 1. Xây dựng SQL cơ bản
        StringBuilder sql = new StringBuilder("SELECT r.* FROM rooms r " +
                "LEFT JOIN service_package sp ON r.service_package_id = sp.id " +
                "WHERE r.status = 'ACTIVE' AND r.expiration_date >= NOW() ");

        // 🔴 FIX LỖI "HIỆN TẤT CẢ": Chia radius cho 111319 để đổi MÉT -> ĐỘ
        if (lat != null && lng != null && radius != null && radius > 0) {
            sql.append(" AND ST_DWithin(r.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :radius / 111319) ");
        }

        // Full Text Search
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND r.search_vector @@ plainto_tsquery('simple', :keyword) ");
        }

        // Các bộ lọc khác (Giữ nguyên)
        if (type != null && !type.equals("ALL") && !type.isEmpty()) sql.append(" AND r.rental_type = :type ");
        if (minPrice != null) sql.append(" AND r.price >= :minPrice ");
        if (maxPrice != null) sql.append(" AND r.price <= :maxPrice ");
        if (minArea != null) sql.append(" AND r.area >= :minArea ");
        if (maxArea != null) sql.append(" AND r.area <= :maxArea ");
        if (bedroomList != null && !bedroomList.isEmpty()) sql.append(" AND r.num_bedrooms IN (:bedroomList) ");
        if (bathroomList != null && !bathroomList.isEmpty()) sql.append(" AND r.num_bathrooms IN (:bathroomList) ");
        if (directionList != null && !directionList.isEmpty()) sql.append(" AND r.direction IN (:directionList) ");
        if (furniture != null && !furniture.trim().isEmpty()) sql.append(" AND r.furniture_status = :furniture ");

        // 2. Query Count
        Query countQuery = entityManager.createNativeQuery("SELECT count(*) FROM (" + sql.toString() + ") as tmp");
        setParameters(countQuery, lat, lng, radius, keyword, type, minPrice, maxPrice, minArea, maxArea, bedroomList, bathroomList, directionList, furniture);
        int totalRows = ((Number) countQuery.getSingleResult()).intValue();

        // 3. Query Data & Sắp xếp (Order By)
        // 🟢 LOGIC SẮP XẾP THÔNG MINH:
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Ưu tiên 1: Độ khớp từ khóa
            sql.append(" ORDER BY ts_rank(r.search_vector, plainto_tsquery('simple', :keyword)) DESC, ");
        } else if (lat != null && lng != null) {
            // Ưu tiên 1: Khoảng cách (nếu không tìm từ khóa)
            sql.append(" ORDER BY ST_Distance(r.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) ASC, ");
        } else {
            // Mặc định
            sql.append(" ORDER BY ");
        }

        // Ưu tiên 2: Tin VIP -> Tin mới đẩy -> Tin mới tạo
        sql.append(" COALESCE(sp.priority_level, 0) DESC, r.last_pushed_at DESC NULLS LAST, r.created_at DESC ");

        Query query = entityManager.createNativeQuery(sql.toString(), Room.class);
        setParameters(query, lat, lng, radius, keyword, type, minPrice, maxPrice, minArea, maxArea, bedroomList, bathroomList, directionList, furniture);

        // Phân trang
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Room> rooms = query.getResultList();

        return new PageImpl<>(rooms.stream().map(roomMapper::toResponse).collect(Collectors.toList()), pageable, totalRows);
    }

    private void setParameters(Query query, Double lat, Double lng, Double radius, String keyword, String type,
                               BigDecimal minPrice, BigDecimal maxPrice, Double minArea, Double maxArea,
                               List<Integer> bedroomList, List<Integer> bathroomList, List<String> directionList, String furniture) {

        if (lat != null && lng != null && radius != null && radius > 0) {
            query.setParameter("lat", lat);
            query.setParameter("lng", lng);
            query.setParameter("radius", radius);
        }


        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", keyword.trim());
        }

        if (type != null && !type.equals("ALL") && !type.isEmpty()) query.setParameter("type", type);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (minArea != null) query.setParameter("minArea", minArea);
        if (maxArea != null) query.setParameter("maxArea", maxArea);
        if (bedroomList != null && !bedroomList.isEmpty()) query.setParameter("bedroomList", bedroomList);
        if (bathroomList != null && !bathroomList.isEmpty()) query.setParameter("bathroomList", bathroomList);
        if (directionList != null && !directionList.isEmpty()) query.setParameter("directionList", directionList);
        if (furniture != null && !furniture.trim().isEmpty()) query.setParameter("furniture", furniture);
    }
}

