package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import com.smartrental.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // 1. Đặt lịch (Khách thuê)
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody @Valid AppointmentCreateDTO dto) {
        return ResponseEntity.ok(appointmentService.createAppointment(dto));
    }

    // 2. Lấy danh sách lịch của tôi (Cả Khách và Chủ đều dùng được)
    @GetMapping("/my-calendar")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyCalendar() {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    // 3. Duyệt/Hủy lịch (Chủ trọ dùng)
    // VD: PUT /api/appointments/1/status?status=CONFIRMED
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}