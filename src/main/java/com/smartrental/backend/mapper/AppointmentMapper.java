package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import com.smartrental.backend.entity.Appointment;
import com.smartrental.backend.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    @Autowired private ModelMapper modelMapper;

    public Appointment toEntity(AppointmentCreateDTO dto) {
        return modelMapper.map(dto, Appointment.class);
    }

    public AppointmentResponseDTO toResponse(Appointment entity) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(entity.getId());
        dto.setMeetTime(entity.getMeetTime());
        dto.setMessage(entity.getMessage());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());

        // Map thông tin phòng
        if (entity.getRoom() != null) {
            dto.setRoomId(entity.getRoom().getId());
            dto.setRoomTitle(entity.getRoom().getTitle());
            dto.setRoomAddress(entity.getRoom().getAddress());
        }

        // Map thông tin đối tác (Cho đơn giản: Trả về tên người tạo lịch)
        if (entity.getTenant() != null) {
            dto.setPartnerName(entity.getTenant().getFullName());
            dto.setPartnerPhone(entity.getTenant().getPhone());
        }
        return dto;
    }
}