package com.smartrental.backend.controller;

import com.smartrental.backend.entity.SearchHistory;
import com.smartrental.backend.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService historyService;

    // 1. Lấy danh sách lịch sử
    @GetMapping
    public ResponseEntity<List<SearchHistory>> getMyHistory() {
        return ResponseEntity.ok(historyService.getMyHistory());
    }

    // 2. Xóa 1 dòng lịch sử
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa lịch sử tìm kiếm"));
    }

    // 3. Xóa tất cả
    @DeleteMapping("/all")
    public ResponseEntity<?> clearAllHistory() {
        historyService.clearAllHistory();
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ lịch sử"));
    }
}