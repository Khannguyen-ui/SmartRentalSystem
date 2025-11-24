package com.smartrental.backend.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private BigDecimal walletBalance;
}