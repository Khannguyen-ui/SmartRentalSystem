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

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        Room room = Room.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .deposit(dto.getDeposit())
                .area(dto.getArea())
                .address(dto.getAddress())
                .servicePackageId(dto.getServicePackageId())
                .rentalType(dto.getRentalType())
                .capacity(dto.getCapacity())
                .genderConstraint(dto.getGenderConstraint())
                .currentTenants(0)
                .location(point)
                .images(dto.getImages())
                .amenities(dto.getAmenities())
                .videoUrl(dto.getVideoUrl())
                .status(Room.Status.PENDING)
                .expirationDate(LocalDateTime.now())
                .landlord(landlord)
                .build();

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public List<RoomResponseDTO> searchNearby(double lat, double lng, double radius) {
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

    // --- BỔ SUNG ĐOẠN NÀY ---
    @Override
    public RoomResponseDTO getRoomDetail(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        return roomMapper.toResponse(room);
    }
}