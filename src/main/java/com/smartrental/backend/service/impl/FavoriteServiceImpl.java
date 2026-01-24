package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.mapper.RoomMapper;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.FavoriteService; // Import interface mới
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService { // Thực thi interface
    private final FavoriteRepository favoriteRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long roomId) {
        User user = getCurrentUser();
        return favoriteRepository.findByUserIdAndRoomId(user.getId(), roomId)
                .map(f -> {
                    favoriteRepository.delete(f);
                    return false;
                })
                .orElseGet(() -> {
                    Room room = roomRepository.findById(roomId)
                            .orElseThrow(() -> new RuntimeException("Room not found"));
                    favoriteRepository.save(Favorite.builder().user(user).room(room).build());
                    return true;
                });
    }

    @Override
    public boolean isFavorited(Long roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nhanh: Nếu auth null hoặc là anonymous thì trả về false luôn
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return false;
        }

        try {
            User user = getCurrentUser();
            return favoriteRepository.existsByUserIdAndRoomId(user.getId(), roomId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<RoomResponseDTO> getMyFavorites() {
        User user = getCurrentUser();
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(f -> roomMapper.toResponse(f.getRoom()))
                .toList();
    }
}