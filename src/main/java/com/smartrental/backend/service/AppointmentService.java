package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import java.util.List;

public interface AppointmentService {
    // Khách đặt lịch
    AppointmentResponseDTO createAppointment(AppointmentCreateDTO dto);
    
    // Chủ trọ duyệt/hủy lịch (status: CONFIRMED / CANCELLED)
    AppointmentResponseDTO updateStatus(Long id, String status);

    // Lấy danh sách lịch của tôi (Dùng chung cho cả Tenant và Landlord)
    List<AppointmentResponseDTO> getMyAppointments();
}