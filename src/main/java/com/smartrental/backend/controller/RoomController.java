package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.RoomCreateDTO;
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

    // 1. Đăng tin (Yêu cầu Token Chủ trọ)
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody @Valid RoomCreateDTO dto) {
        return ResponseEntity.ok(roomService.createRoom(dto));
    }

    // 2. Tìm kiếm quanh đây (Công khai)
    // URL: /api/rooms/search?lat=10.7&lng=106.6&radius=5000
    @GetMapping("/search")
    public ResponseEntity<List<RoomResponseDTO>> searchRooms(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") double radius) {
        return ResponseEntity.ok(roomService.searchNearby(lat, lng, radius));
    }

    // 3. Lấy phòng của tôi (Cho trang quản lý chủ trọ)
    @GetMapping("/my-rooms")
    public ResponseEntity<List<RoomResponseDTO>> getMyRooms() {
        return ResponseEntity.ok(roomService.getMyRooms());
    }
    
    // 4. Xóa phòng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Xóa phòng thành công");
    }
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }
}