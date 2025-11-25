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
        RoomResponseDTO dto = modelMapper.map(room, RoomResponseDTO.class);
        
        // Map thủ công tọa độ từ Point sang Double
        if (room.getLocation() != null) {
            dto.setLatitude(room.getLocation().getY());  // Vĩ độ
            dto.setLongitude(room.getLocation().getX()); // Kinh độ
        }
        
        // Map thông tin chủ trọ
        if (room.getLandlord() != null) {
            dto.setLandlordName(room.getLandlord().getFullName());
            dto.setLandlordPhone(room.getLandlord().getPhone());
        }
        
        return dto;
    }
}