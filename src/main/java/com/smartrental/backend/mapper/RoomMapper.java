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

    public RoomResponseDTO toResponse(Room room) {
        RoomResponseDTO dto = modelMapper.map(room, RoomResponseDTO.class);

        // 🟢 THÊM: Đảm bảo dữ liệu VIP được truyền sang DTO
        dto.setPackageType(room.getPackageType());
        dto.setPriorityLevel(room.getPriorityLevel());

        // Map tọa độ
        if (room.getLocation() != null) {
            dto.setLatitude(room.getLocation().getY());
            dto.setLongitude(room.getLocation().getX());
        }

        // Map thông tin chủ trọ
        if (room.getLandlord() != null) {
            dto.setLandlordId(room.getLandlord().getId());
            dto.setLandlordName(room.getLandlord().getFullName());
            dto.setLandlordPhone(room.getLandlord().getPhone());
            dto.setLandlordAvatar(room.getLandlord().getAvatarUrl());
            dto.setLandlordJoinDate(room.getLandlord().getCreatedAt());
        } else {
            dto.setLandlordName("Hệ thống / Admin");
            dto.setLandlordId(0L);
        }

        dto.setCreatedAt(room.getCreatedAt());
        return dto;
    }
}