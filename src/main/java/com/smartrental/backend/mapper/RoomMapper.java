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

    // Không dùng toEntity này trong createRoom nữa vì mình đã dùng Builder ở Service rồi
    // Giữ lại nếu cần dùng cho Update Room sau này
    public Room toEntity(RoomCreateDTO dto) {
        return modelMapper.map(dto, Room.class);
    }

    public RoomResponseDTO toResponse(Room room) {
        RoomResponseDTO dto = modelMapper.map(room, RoomResponseDTO.class);

        // Map tọa độ
        if (room.getLocation() != null) {
            dto.setLatitude(room.getLocation().getY());
            dto.setLongitude(room.getLocation().getX());
        }

        // Map thông tin chủ trọ
        if (room.getLandlord() != null) {
            dto.setLandlordName(room.getLandlord().getFullName());
            dto.setLandlordPhone(room.getLandlord().getPhone());
        }

        // ModelMapper thường tự map Enum nếu tên giống nhau.
        // Nhưng nếu muốn chắc chắn, bạn có thể set tay:
        // dto.setRentalType(room.getRentalType());

        return dto;
    }
}