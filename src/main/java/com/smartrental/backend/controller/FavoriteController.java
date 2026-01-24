package com.smartrental.backend.controller;

import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.service.FavoriteService; // Import Interface
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {


    private final FavoriteService favoriteService;

    @PostMapping("/{roomId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long roomId) {
        boolean isFavorited = favoriteService.toggleFavorite(roomId);
        return ResponseEntity.ok(Map.of("isFavorited", isFavorited));
    }

    // Endpoint bạn đang gặp lỗi 404
    @GetMapping("/check/{roomId}")
    public ResponseEntity<Boolean> checkStatus(@PathVariable Long roomId) {
        return ResponseEntity.ok(favoriteService.isFavorited(roomId));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getMyFavorites() {
        return ResponseEntity.ok(favoriteService.getMyFavorites());
    }
}