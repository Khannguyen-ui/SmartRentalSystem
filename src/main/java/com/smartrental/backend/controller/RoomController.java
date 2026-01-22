package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.RoomCreateDTO;
import com.smartrental.backend.dto.request.RoomUpdateDTO; // Cần tạo DTO này
import com.smartrental.backend.dto.response.RoomResponseDTO;
import com.smartrental.backend.service.SearchHistoryService;
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
    private final SearchHistoryService searchHistoryService;
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
            @RequestParam(name = "radius", defaultValue = "50000") String radiusStr,

            // 👇 SỬA DÒNG NÀY 👇
            // Ý nghĩa: "Lấy tham số 'address' trên URL, nhưng gán vào biến tên là 'keyword'"
            @RequestParam(name = "address", required = false) String keyword
    ) {
        try {
            Double lat = Double.parseDouble(latStr);
            Double lng = Double.parseDouble(lngStr);
            Double radius = Double.parseDouble(radiusStr);

            // 1. Lưu lịch sử
            if (keyword != null && !keyword.trim().isEmpty()) {
                try {
                    // Lưu từ khóa này vào lịch sử (dù nó là tên đường hay tên phòng)
                    searchHistoryService.saveSearch(keyword, lat, lng, radius);
                }catch (Exception e) {
                    System.out.println("LỖI LƯU LỊCH SỬ: " + e.getMessage());
                    e.printStackTrace(); // In chi tiết lỗi ra console để debug
                }
            }

            // 2. Gọi Service (Lúc này biến tên là keyword, nghe hợp lý hơn hẳn)
            return ResponseEntity.ok(roomService.searchNearby(lat, lng, radius, keyword));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByLandlord(@PathVariable Long landlordId) {
        return ResponseEntity.ok(roomService.getRoomsByLandlord(landlordId));
    }


}