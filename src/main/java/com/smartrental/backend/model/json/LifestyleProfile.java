package com.smartrental.backend.model.json;

import lombok.Data;

@Data
public class LifestyleProfile {
    private String sleepTime;     // VD: "23:00
    private boolean hasPet;       // Có nuôi thú cưng không
    private boolean smoking;      // Có hút thuốc không
    private int cleanlinessLevel; // Độ sạch sẽ (1-5)
    private String personality;   // Introvert / Extrovert
}