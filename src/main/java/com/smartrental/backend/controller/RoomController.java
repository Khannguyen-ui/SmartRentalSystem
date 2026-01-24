package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO; // Cần tạo DTO này
import com.smartrental.backend.dto.response.PriceHistoryDTO;
import com.smartrental.backend.dto.response.PriceTrendResponse;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.service.SearchHistoryService;
import com.smartrental.backend.service.RoomService;
import com.smartrental.backend.service.PriceStatisticsService; // Thêm dòng này

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final SearchHistoryService searchHistoryService;
    private final RoomService roomService;
    private final PriceStatisticsService priceStatisticsService;

    // 1. Đăng tin
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody @Valid RoomCreateDTO dto) {
        return ResponseEntity.ok(roomService.createRoom(dto));
    }

    // 2. Cập nhật phòng (MỚI)
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Long id,
            @RequestBody @Valid RoomUpdateDTO dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    // 3. Xóa phòng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Đã xóa phòng và toàn bộ ảnh liên quan");
    }

    // 4. Lấy chi tiết phòng (Bao gồm danh sách ảnh)
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }

    // 5. Lấy danh sách phòng của tôi
    @GetMapping("/my-rooms")
    public ResponseEntity<List<RoomResponseDTO>> getMyRooms() {
        return ResponseEntity.ok(roomService.getMyRooms());
    }

    // 6. Tìm kiếm (Giữ nguyên)
    @GetMapping("/search")
    public ResponseEntity<?> searchRooms(
            @RequestParam(name = "lat",required = false) Double lat,
            @RequestParam(name = "lng",required = false) Double lng,
            @RequestParam(name = "radius", defaultValue = "50000") Double radius,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "address", required = false) String address,
            // 👇 THÊM 2 THAM SỐ NÀY 👇
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        try {
            // 1. Lưu lịch sử (Giữ nguyên logic của bạn)
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchHistoryService.saveSearch(keyword, address, lat, lng, radius);
            }

            // 2. Tạo đối tượng Pageable
            Pageable pageable = PageRequest.of(page, size);

            // 3. Gọi Service và trả về Page thay vì List
            Page<RoomResponseDTO> result = roomService.searchNearby(lat, lng, radius, keyword, pageable);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByLandlord(@PathVariable Long landlordId) {
        return ResponseEntity.ok(roomService.getRoomsByLandlord(landlordId));

    }
    @GetMapping("/{id}/price-history")
// Thay đổi List<PriceHistoryDTO> thành PriceTrendResponse
    public ResponseEntity<PriceTrendResponse> getPriceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(priceStatisticsService.getPriceHistoryForRoom(id));
    }
    // 7. Lấy danh sách tin có Video (MỚI)
    @GetMapping("/videos")
    public ResponseEntity<Page<RoomResponseDTO>> getRoomsWithVideo(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(roomService.getRoomsWithVideo(pageable));
    }


}