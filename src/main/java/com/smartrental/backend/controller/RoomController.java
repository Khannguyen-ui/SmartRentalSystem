package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO; // Cần tạo DTO này
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

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
            @RequestParam(name = "lat") String latStr,
            @RequestParam(name = "lng") String lngStr,
            @RequestParam(name = "radius", defaultValue = "50000") String radiusStr
    ) {
        try {
            // Tự ép kiểu thủ công -> Bắt lỗi dễ dàng hơn
            Double lat = Double.parseDouble(latStr);
            Double lng = Double.parseDouble(lngStr);
            Double radius = Double.parseDouble(radiusStr);

            return ResponseEntity.ok(roomService.searchNearby(lat, lng, radius));
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra log để xem
            return ResponseEntity.badRequest().body("Lỗi tham số: " + e.getMessage());
        }
    }


}