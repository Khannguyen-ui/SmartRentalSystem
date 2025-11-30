package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ServiceOrderDTO;
import com.smartrental.backend.entity.Partner;
import com.smartrental.backend.entity.ServiceOrder;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.PartnerRepository;
import com.smartrental.backend.repository.ServiceOrderRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.ServiceBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceBookingServiceImpl implements ServiceBookingService {

    private final PartnerRepository partnerRepository;
    private final ServiceOrderRepository orderRepository;
    private final UserRepository userRepository;

    // Helper: Lấy user hiện tại đang đăng nhập
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- PARTNER LOGIC (QUẢN LÝ ĐỐI TÁC) ---

    @Override
    @Transactional
    public Partner createPartner(Partner partner) {
        // Thường chức năng này dành cho Admin (đã được chặn quyền ở Controller)
        partner.setActive(true); // Mặc định kích hoạt khi tạo mới
        return partnerRepository.save(partner);
    }

    @Override
    public List<Partner> getAllPartners() {
        // Lấy danh sách các đối tác đang hoạt động (Active = true)
        // Dùng để hiển thị cho người thuê chọn
        return partnerRepository.findByIsActiveTrue();
    }

    // --- BOOKING LOGIC (ĐẶT DỊCH VỤ) ---

    @Override
    @Transactional
    public ServiceOrderDTO bookService(ServiceOrderDTO dto) {
        User tenant = getCurrentUser();

        // 1. Tìm đối tác dịch vụ theo ID
        Partner partner = partnerRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Đối tác dịch vụ không tồn tại"));

        // 2. Tạo đơn đặt hàng mới
        ServiceOrder order = ServiceOrder.builder()
                .tenant(tenant)
                .partner(partner)
                .bookingTime(dto.getBookingTime())
                .note(dto.getNote())
                .status("PENDING") // Trạng thái mặc định là Chờ xác nhận
                .build();

        ServiceOrder savedOrder = orderRepository.save(order);

        // 3. Chuyển đổi Entity sang DTO để trả về kết quả
        ServiceOrderDTO responseDTO = new ServiceOrderDTO();
        responseDTO.setId(savedOrder.getId());
        responseDTO.setPartnerId(partner.getId());
        responseDTO.setPartnerName(partner.getName());
        responseDTO.setBookingTime(savedOrder.getBookingTime());
        responseDTO.setNote(savedOrder.getNote());
        responseDTO.setStatus(savedOrder.getStatus());

        return responseDTO;
    }

    @Override
    public List<ServiceOrderDTO> getMyOrders() {
        User tenant = getCurrentUser();

        // Lấy lịch sử đặt dịch vụ của người dùng hiện tại, sắp xếp mới nhất lên đầu
        return orderRepository.findByTenantIdOrderByCreatedAtDesc(tenant.getId())
                .stream()
                .map(order -> {
                    ServiceOrderDTO dto = new ServiceOrderDTO();
                    dto.setId(order.getId());
                    dto.setPartnerId(order.getPartner().getId());
                    dto.setPartnerName(order.getPartner().getName()); // Hiển thị tên đối tác (VD: Thành Hưng)
                    dto.setBookingTime(order.getBookingTime());
                    dto.setNote(order.getNote());
                    dto.setStatus(order.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}