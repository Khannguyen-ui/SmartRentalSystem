package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.MarketItemDTO;
import com.smartrental.backend.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    // 1. Đăng bán đồ cũ (Tenant)
    // POST /api/market
    @PostMapping
    public ResponseEntity<MarketItemDTO> createItem(@RequestBody MarketItemDTO dto) {
        return ResponseEntity.ok(marketService.createItem(dto));
    }

    // 2. Xem danh sách đồ đang bán (Public/Tenant)
    // GET /api/market
    @GetMapping
    public ResponseEntity<List<MarketItemDTO>> getAllItems() {
        return ResponseEntity.ok(marketService.getAllAvailableItems());
    }

    // 3. Tìm kiếm đồ cũ
    // GET /api/market/search?keyword=tủ lạnh
    @GetMapping("/search")
    public ResponseEntity<List<MarketItemDTO>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(marketService.searchItems(keyword));
    }

    // 4. Đánh dấu đã bán & Chốt người mua (Chủ đồ gọi)
    // PUT /api/market/{id}/sold?buyerId=2
    @PutMapping("/{id}/sold")
    public ResponseEntity<?> markAsSold(@PathVariable Long id,
                                        @RequestParam(required = false) Long buyerId) {
        marketService.markAsSold(id, buyerId);
        return ResponseEntity.ok("Đã chốt đơn thành công! Món đồ đã được đánh dấu ĐÃ BÁN.");
    }
}