package com.smartrental.backend.dto.request;

import lombok.Data;

@Data
public class ApproveRequestDTO {
    private boolean isApproved; // true = Duyệt, false = Từ chối
    private String reason; // Lý do từ chối (nếu có)
}