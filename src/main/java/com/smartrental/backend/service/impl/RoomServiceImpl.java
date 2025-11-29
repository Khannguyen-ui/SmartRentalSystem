package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.RoomMapper;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

        // Tạo Point từ tọa độ
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        // Map thủ công hoặc dùng Builder để đảm bảo các trường Hybrid được set đúng
        Room room = Room.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .deposit(dto.getDeposit())
                .area(dto.getArea())
                .address(dto.getAddress())

                // --- FIELDS HYBRID ---
                .rentalType(dto.getRentalType())
                .capacity(dto.getCapacity())
                .genderConstraint(dto.getGenderConstraint())
                .currentTenants(0) // Mặc định chưa có ai
                // ---------------------

                .location(point)
                .images(dto.getImages())
                .amenities(dto.getAmenities())
                .status(Room.Status.ACTIVE)
                .expirationDate(LocalDateTime.now().plusDays(30))
                .landlord(landlord)
                .build();

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public List<RoomResponseDTO> searchNearby(double lat, double lng, double radius) {
        // Lưu ý thứ tự tham số: Repository đang define (lat, lng) nhưng ST_MakePoint cần (lng, lat)
        // Code Repository của bạn đã đúng logic: ST_MakePoint(:longitude, :latitude)
        List<Room> rooms = roomRepository.findRoomsNearby(lat, lng, radius);
        return rooms.stream().map(roomMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getMyRooms() {
        User landlord = getCurrentUser();
        return roomRepository.findByLandlordId(landlord.getId())
                .stream().map(roomMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteRoom(Long id) {
        User landlord = getCurrentUser();
        Room room = roomRepository.findByIdAndLandlordId(id, landlord.getId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại hoặc không phải của bạn"));
        roomRepository.delete(room);
    }
}