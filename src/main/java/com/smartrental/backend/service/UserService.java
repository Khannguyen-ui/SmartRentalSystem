package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO register(UserRegisterDTO registerDTO);
    AuthResponse login(LoginDTO loginDTO);
}