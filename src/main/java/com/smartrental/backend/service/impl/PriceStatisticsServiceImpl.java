package com.smartrental.backend.service.impl; // Đúng package service.impl

import com.smartrental.backend.dto.response.PriceHistoryDTO;
import com.smartrental.backend.dto.response.PriceTrendResponse;
import com.smartrental.backend.entity.PriceTrend;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.repository.PriceTrendRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.service.PriceStatisticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
// QUAN TRỌNG: Lớp là PriceStatisticsServiceImpl, Interface là PriceStatisticsService
public class PriceStatisticsServiceImpl implements PriceStatisticsService {

    private final RoomRepository roomRepository;
    private final PriceTrendRepository trendRepository;
    private String mapRoomTypeToTrendType(String roomRentalType) {
        if (roomRentalType == null) return null;

        return switch (roomRentalType) {
            case "PHONG_TRO", "SHARED_ROOM", "ROOM" -> "SHARED";
            case "NGUYEN_CAN", "HOUSE", "WHOLE_HOUSE" -> "WHOLE";
            default -> roomRentalType;
        };
    }


    @Override
    public PriceTrendResponse getPriceHistoryForRoom(Long roomId) {
        // 1. Lấy thông tin phòng để lấy giá hiện tại
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        String trendRentalType = mapRoomTypeToTrendType(
                room.getRentalType().name()
        );

        // 2. Lấy danh sách lịch sử từ Repository
        List<PriceHistoryDTO> history = trendRepository.findNearbyTrends(
                room.getLocation(),
                0.02,
                room.getRentalType()
        );

        // 3. Đóng gói vào đối tượng Response mới
        return PriceTrendResponse.builder()
                .currentRoomPrice(room.getPrice()) // Đây là phần "giá hiện tại" bạn cần bổ sung
                .history(history)
                .build();
    }
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional
    public void generateMonthlyTrends() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // Bước 1: Chỉ lấy những phòng ở khu vực CHƯA có dữ liệu xu hướng tháng này
        List<Room> roomsToProcess = roomRepository.findRoomsNeedingTrendUpdate(month, year);

        for (Room room : roomsToProcess) {
            String trendRentalType = mapRoomTypeToTrendType(
                    room.getRentalType().name()
            );
            // Bước 2: Vì SQL đã lọc rồi, ở đây chúng ta tính toán luôn không cần check exists nữa
            Map<String, Object> stats = roomRepository.calculateStatsAroundPoint(
                    room.getLocation(),
                    2000.0,
                    room.getRentalType().name()
            );

            if (stats != null && stats.get("avg_p") != null) {
                PriceTrend trend = PriceTrend.builder()
                        .areaCenter(room.getLocation())
                        .month(month)
                        .year(year)
                        .minPrice(new BigDecimal(stats.get("min_p").toString()))
                        .avgPrice(new BigDecimal(stats.get("avg_p").toString()))
                        .maxPrice(new BigDecimal(stats.get("max_p").toString()))
                        .rentalType(room.getRentalType())
                        .build();
                trendRepository.save(trend);
            }
        }

    }
}
