package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // API Tạo hợp đồng (Chủ trọ gọi)
    @PostMapping
    public ResponseEntity<ContractResponseDTO> createContract(@RequestBody @Valid ContractCreateDTO dto) {
        return ResponseEntity.ok(contractService.createContract(dto));
    }
}