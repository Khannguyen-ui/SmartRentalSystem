package com.smartrental.backend.service.impl; // Đúng package service.impl

import com.smartrental.backend.dto.response.PriceHistoryDTO;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.repository.PriceTrendRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.service.PriceStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
// QUAN TRỌNG: Lớp là PriceStatisticsServiceImpl, Interface là PriceStatisticsService
public class PriceStatisticsServiceImpl implements PriceStatisticsService {

    private final RoomRepository roomRepository;
    private final PriceTrendRepository trendRepository;

    @Override
    public List<PriceHistoryDTO> getPriceHistoryForRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        // Sử dụng tọa độ POINT và loại hình thuê từ Entity Room
        return trendRepository.findNearbyTrends(
                room.getLocation(),
                0.02,
                room.getRentalType()
        );
    }
}