package com.smartrental.backend.service;

import com.smartrental.backend.dto.response.RoomResponseDTO;
import java.util.List;

public interface FavoriteService {
    boolean toggleFavorite(Long roomId);
    boolean isFavorited(Long roomId);
    List<RoomResponseDTO> getMyFavorites();
}