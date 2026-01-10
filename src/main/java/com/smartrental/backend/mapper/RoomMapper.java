package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.Room;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    @Autowired
    private ModelMapper modelMapper;

    public Room toEntity(RoomCreateDTO dto) {
        return modelMapper.map(dto, Room.class);
    }

    public RoomResponseDTO toResponse(Room room) {
        // 1. Tự động map các trường trùng tên (bao gồm cả furnitureStatus, numBedrooms... nếu Entity có)
        RoomResponseDTO dto = modelMapper.map(room, RoomResponseDTO.class);

        // 2. Map tọa độ (Check null để an toàn)
        if (room.getLocation() != null) {
            dto.setLatitude(room.getLocation().getY());
            dto.setLongitude(room.getLocation().getX());
        }

        // 3. Map thông tin chủ trọ (Check null để tránh lỗi 500/400)
        if (room.getLandlord() != null) {
            dto.setLandlordId(room.getLandlord().getId());
            dto.setLandlordName(room.getLandlord().getFullName());
            dto.setLandlordPhone(room.getLandlord().getPhone());
            dto.setLandlordAvatar(room.getLandlord().getAvatarUrl());
            dto.setLandlordJoinDate(room.getLandlord().getCreatedAt());
        } else {
            // Fallback nếu không có chủ trọ (tránh crash app)
            dto.setLandlordName("Hệ thống / Admin");
            dto.setLandlordId(0L);
        }

        // 4. Map ngày tạo bài viết
        dto.setCreatedAt(room.getCreatedAt());

        return dto;
    }
}