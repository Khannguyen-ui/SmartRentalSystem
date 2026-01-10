package com.smartrental.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LandlordStatsDTO {
    private Long id;
    private String name;
    private String avatar;
    private Long postCount;
}