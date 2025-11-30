package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ContractCreateDTO;
import com.smartrental.backend.dto.response.ContractResponseDTO;
import com.smartrental.backend.service.ContractService;
import com.smartrental.backend.service.PdfService; // Import Service mới
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final PdfService pdfService;

    // 1. Tạo hợp đồng (Giữ nguyên)
    @PostMapping
    public ResponseEntity<ContractResponseDTO> createContract(@RequestBody @Valid ContractCreateDTO dto) {
        return ResponseEntity.ok(contractService.createContract(dto));
    }

    // 2. Xuất file PDF (API MỚI)
    // GET /api/contracts/1/pdf
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) throws IOException {
        byte[] pdfBytes = pdfService.generateContractPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}