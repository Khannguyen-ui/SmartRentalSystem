package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.LoginDTO;
import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.AuthResponse;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.UserMapper;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.UserService;
import com.smartrental.backend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
        // 1. Check trùng email
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        // 2. Convert DTO -> Entity
        User user = userMapper.toEntity(registerDTO);

        // 3. Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        // 4. Set giá trị mặc định
        user.setWalletBalance(java.math.BigDecimal.ZERO);
        user.setKycStatus("UNVERIFIED");

        // 5. Lưu DB
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginDTO loginDTO) {
        // 1. Xác thực Username/Password
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );
        } catch (Exception e) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng!");
        }

        // 2. Tìm User
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Sinh Token
        String token = jwtUtils.generateToken(user);

        // 4. Trả về Response
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}