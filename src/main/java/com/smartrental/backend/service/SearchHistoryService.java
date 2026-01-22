package com.smartrental.backend.service;

import com.smartrental.backend.entity.SearchHistory;
import java.util.List;

public interface SearchHistoryService {
    // Đổi queryText thành address cho đồng bộ
    void saveSearch(String address, Double lat, Double lng, Double radius);

    List<SearchHistory> getMyHistory();
    void deleteHistory(Long id);
    void clearAllHistory();
}