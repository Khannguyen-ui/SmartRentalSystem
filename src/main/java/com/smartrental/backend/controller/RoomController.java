package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO;
import com.smartrental.backend.dto.response.PriceTrendResponse;
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.service.PriceStatisticsService;
import com.smartrental.backend.service.RoomService;
import com.smartrental.backend.service.SearchHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import thêm Map

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

    // 2. Cập nhật phòng
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

    // 4. Lấy chi tiết phòng
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }

    // 5. Lấy danh sách phòng của tôi
    @GetMapping("/my-rooms")
    public ResponseEntity<List<RoomResponseDTO>> getMyRooms() {
        return ResponseEntity.ok(roomService.getMyRooms());
    }

    // 6. Tìm kiếm
    @GetMapping("/search")
    public ResponseEntity<?> searchRooms(
            @RequestParam(name = "lat",required = false) Double lat,
            @RequestParam(name = "lng",required = false) Double lng,
            @RequestParam(name = "radius", defaultValue = "50000") Double radius,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size
    ) {
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchHistoryService.saveSearch(keyword, address, lat, lng, radius);
            }
            Pageable pageable = PageRequest.of(page, size);
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
    public ResponseEntity<PriceTrendResponse> getPriceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(priceStatisticsService.getPriceHistoryForRoom(id));
    }

    // 7. Lấy danh sách tin có Video
    @GetMapping("/videos")
    public ResponseEntity<Page<RoomResponseDTO>> getRoomsWithVideo(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(roomService.getRoomsWithVideo(pageable));
    }

    // ==========================================
    // (MỚI) CÁC API ĐẨY TIN & QUẢN LÝ TRẠNG THÁI
    // ==========================================

    // 8. Đẩy tin lên Top (Mua gói)
    @PostMapping("/{id}/push")
    public ResponseEntity<?> pushRoom(
            @PathVariable Long id,
            @RequestParam Long packageId) {
        try {
            roomService.pushRoomToTop(id, packageId);
            return ResponseEntity.ok(Map.of("message", "Đẩy tin thành công! Tin của bạn đã lên Top."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 9. Ẩn / Hiện tin (Hạ tin)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) { // status = "HIDDEN" hoặc "ACTIVE"
        try {
            roomService.updateRoomStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công: " + status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 10. Bật / Tắt tự động gia hạn
    @PutMapping("/{id}/auto-renew")
    public ResponseEntity<?> toggleAutoRenew(
            @PathVariable Long id,
            @RequestParam boolean enable) {
        try {
            roomService.toggleAutoRenew(id, enable);
            return ResponseEntity.ok(Map.of("message", enable ? "Đã BẬT tự động gia hạn" : "Đã TẮT tự động gia hạn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}