package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ServiceOrderDTO;
import com.smartrental.backend.entity.Partner;
import com.smartrental.backend.service.impl.ServiceBookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services") // Đây là gốc đường dẫn
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceBookingServiceImpl bookingService;

    // 1. Tạo đối tác (Admin) -> URL: /api/services/partners
    @PostMapping("/partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Partner> createPartner(@RequestBody Partner partner) {
        return ResponseEntity.ok(bookingService.createPartner(partner));
    }

    // 2. Xem danh sách đối tác -> URL: /api/services/partners
    @GetMapping("/partners")
    public ResponseEntity<List<Partner>> getPartners() {
        return ResponseEntity.ok(bookingService.getAllPartners());
    }

    // 3. Đặt dịch vụ -> URL: /api/services/book
    @PostMapping("/book")
    public ResponseEntity<ServiceOrderDTO> bookService(@RequestBody ServiceOrderDTO dto) {
        return ResponseEntity.ok(bookingService.bookService(dto));
    }
}