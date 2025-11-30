package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ServiceOrderDTO;
import com.smartrental.backend.entity.Partner;
import java.util.List;

public interface ServiceBookingService {
    // --- PARTNER (Đối tác) ---
    Partner createPartner(Partner partner); // Admin tạo
    List<Partner> getAllPartners(); // Tenant xem danh sách

    // --- BOOKING (Đặt đơn) ---
    ServiceOrderDTO bookService(ServiceOrderDTO dto); // Tenant đặt
    List<ServiceOrderDTO> getMyOrders(); // Tenant xem lịch sử đặt
}