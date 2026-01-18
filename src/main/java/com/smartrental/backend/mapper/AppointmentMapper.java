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

    @Autowired
    private ModelMapper modelMapper;

    // Chuyển từ Request DTO sang Entity (Dùng khi tạo mới)
    public Appointment toEntity(AppointmentCreateDTO dto) {
        return modelMapper.map(dto, Appointment.class);
    }

    /**
     * Chuyển từ Entity sang Response DTO
     * Có xử lý logic C2C và kiểm tra Null an toàn
     *
     * @param entity: Lịch hẹn lấy từ DB
     * @param currentUserId: ID của người đang xem API này
     */
    public AppointmentResponseDTO toResponse(Appointment entity, Long currentUserId) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();

        // 1. Map các trường cơ bản
        dto.setId(entity.getId());
        dto.setMeetTime(entity.getMeetTime());
        dto.setSuggestedMeetTime(entity.getSuggestedMeetTime());
        dto.setMessage(entity.getMessage());
        // Check null status phòng khi dữ liệu cũ
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "PENDING");
        dto.setCreatedAt(entity.getCreatedAt());

        // 2. Map thông tin phòng (CHECK NULL AN TOÀN)
        if (entity.getRoom() != null) {
            dto.setRoomId(entity.getRoom().getId());
            dto.setRoomTitle(entity.getRoom().getTitle());
            dto.setRoomAddress(entity.getRoom().getAddress());
        } else {
            dto.setRoomId(null);
            dto.setRoomTitle("Phòng không tồn tại hoặc đã bị xóa");
            dto.setRoomAddress("N/A");
        }

        // 3. Kiểm tra Tenant có tồn tại không (Tránh NPE nếu user bị xóa)
        if (entity.getTenant() == null) {
            dto.setPartnerName("Người dùng đã bị xóa");
            return dto;
        }

        // 4. Xác định vai trò (Tôi là người thuê hay chủ nhà?)
        boolean amITenant = entity.getTenant().getId().equals(currentUserId);
        dto.setMyRequest(amITenant); // Set cờ

        // 5. Map thông tin Đối tác (Partner)
        if (amITenant) {
            // --- TRƯỜNG HỢP A: Tôi là Tenant ---
            // => Đối tác liên hệ là Chủ trọ (Landlord)

            // Cần check kỹ: Phòng có thể null, hoặc Chủ trọ của phòng đó đã bị xóa
            if (entity.getRoom() != null && entity.getRoom().getLandlord() != null) {
                User landlord = entity.getRoom().getLandlord();
                dto.setPartnerId(landlord.getId());
                dto.setPartnerName(landlord.getFullName());
                dto.setPartnerPhone(landlord.getPhone());
                dto.setPartnerAvatar(landlord.getAvatarUrl());
            } else {
                dto.setPartnerName("Chủ trọ không xác định");
                dto.setPartnerPhone("N/A");
            }
        } else {
            // --- TRƯỜNG HỢP B: Tôi là Landlord (hoặc người khác) ---
            // => Đối tác liên hệ là Khách thuê (Tenant)

            // Tenant đã được check null ở bước 3 rồi
            User tenant = entity.getTenant();
            dto.setPartnerId(tenant.getId());
            dto.setPartnerName(tenant.getFullName());
            dto.setPartnerPhone(tenant.getPhone());
            dto.setPartnerAvatar(tenant.getAvatarUrl());
        }

        return dto;
    }

    // (Optional) Hàm toResponse cũ nếu cần dùng cho Admin
    public AppointmentResponseDTO toResponseForAdmin(Appointment entity) {
        // Nếu tenant null thì dùng ID 0 tạm để tránh lỗi
        Long tenantId = (entity.getTenant() != null) ? entity.getTenant().getId() : 0L;
        return toResponse(entity, tenantId);
    }
}