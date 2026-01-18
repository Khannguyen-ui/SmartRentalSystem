package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    /**
     * Khách thuê tạo yêu cầu đặt lịch
     */
    AppointmentResponseDTO createAppointment(AppointmentCreateDTO dto);

    /**
     * Cập nhật trạng thái lịch hẹn (Duyệt/Hủy/Từ chối)
     * Dành cho cả Chủ trọ và Khách thuê (tùy ngữ cảnh)
     */
    AppointmentResponseDTO updateStatus(Long id, String status);

    /**
     * Lấy danh sách lịch của tôi
     * (Bao gồm cả lịch tôi đi thuê và lịch người khác thuê phòng của tôi)
     */
    List<AppointmentResponseDTO> getMyAppointments();

    // Huy lịch hang loạt nếu kí hợp đông cho người hẹn

    void cancelAllAppointmentsForRoom(Long roomId, String reason);

    AppointmentResponseDTO suggestNewTime(Long id, LocalDateTime newTime, String note);

    AppointmentResponseDTO acceptSuggestion(Long id);


}