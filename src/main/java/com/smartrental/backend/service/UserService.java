package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.KycRequestDTO;
import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.LandlordStatsDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO register(UserRegisterDTO registerDTO);
    AuthResponse login(LoginDTO loginDTO);
    List<LandlordStatsDTO> getTopLandlords(double lat, double lng, double radius);
    void upgradeToLandlord();
    void submitKyc(KycRequestDTO dto);
}