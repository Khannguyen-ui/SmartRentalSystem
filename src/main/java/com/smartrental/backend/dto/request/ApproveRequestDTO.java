package com.smartrental.backend.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApproveRequestDTO {
    @JsonProperty("approved")
    private boolean approved;

    private String reason; // Lý do từ chối (nếu có)
}