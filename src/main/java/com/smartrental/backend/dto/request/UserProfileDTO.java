package com.smartrental.backend.dto.request;

import com.smartrental.backend.model.json.LifestyleProfile;
import lombok.Data;

@Data
public class UserProfileDTO {
    private String fullName;
    private String phone;
    private String avatarUrl; // Link Cloudinary
    private String citizenId;

    // Thông tin lối sống (Quan trọng cho Matching)
    private LifestyleProfile lifestyleProfile;
}