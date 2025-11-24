package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.UserRegisterDTO;
import com.smartrental.backend.dto.response.UserResponseDTO;
import com.smartrental.backend.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;

    public User toEntity(UserRegisterDTO dto) {
        User user = modelMapper.map(dto, User.class);
        // Map Role từ String sang Enum
        user.setRole(User.Role.valueOf(dto.getRole().toUpperCase()));
        return user;
    }

    public UserResponseDTO toResponse(User entity) {
        return modelMapper.map(entity, UserResponseDTO.class);
    }
}