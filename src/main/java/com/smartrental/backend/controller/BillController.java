package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.BillCreateDTO;
import com.smartrental.backend.dto.response.BillResponseDTO;
import com.smartrental.backend.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    // API Tạo hóa đơn (Chủ trọ gọi hàng tháng)
    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(@RequestBody @Valid BillCreateDTO dto) {
        return ResponseEntity.ok(billService.createBill(dto));
    }
}