package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.AppointmentCreateDTO;
import com.smartrental.backend.dto.response.AppointmentResponseDTO;
import com.smartrental.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Tenant đặt lịch
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody @Valid AppointmentCreateDTO dto) {
        return ResponseEntity.ok(appointmentService.createAppointment(dto));
    }

    // Lấy lịch của tôi (Trả về cả lịch đi thuê và lịch cho thuê)
    @GetMapping("/my-calendar")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyCalendar() {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    // Duyệt / Hủy / Từ chối
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
    @PutMapping("/{id}/suggest")
    public ResponseEntity<AppointmentResponseDTO> suggestNewTime(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(appointmentService.suggestNewTime(id, newTime, note));
    }

    // 2. Khách đồng ý giờ mới
    // URL: PUT /api/appointments/1/accept-suggestion
    @PutMapping("/{id}/accept-suggestion")
    public ResponseEntity<AppointmentResponseDTO> acceptSuggestion(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.acceptSuggestion(id));
    }

}