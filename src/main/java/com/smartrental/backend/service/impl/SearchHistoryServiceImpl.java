package com.smartrental.backend.service.impl;

import com.smartrental.backend.entity.SearchHistory;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.SearchHistoryRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository historyRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            // Check kỹ hơn để tránh lỗi NullPointerException
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void saveSearch(String queryText, String address, Double lat, Double lng, Double radius) {
        User user = getCurrentUser();
        if (user == null) return;

        // Tìm xem đã từng tìm tổ hợp này chưa
        Optional<SearchHistory> existing = historyRepository.findByUserIdAndQueryText(user.getId(), queryText);

        if (existing.isPresent()) {
            SearchHistory history = existing.get();
            history.setSearchedAt(LocalDateTime.now());
            history.setAddress(address); // Cập nhật địa chỉ mới nhất nếu có
            historyRepository.save(history);
        } else {
            SearchHistory newHistory = SearchHistory.builder()
                    .user(user)
                    .queryText(queryText)
                    .address(address)
                    .latitude(lat)
                    .longitude(lng)
                    .radius(radius)
                    .build();
            historyRepository.save(newHistory);
        }
    }

    @Override
    public List<SearchHistory> getMyHistory() {
        User user = getCurrentUser();
        if (user == null) return List.of();
        return historyRepository.findByUserIdOrderBySearchedAtDesc(user.getId());
    }

    @Override
    @Transactional
    public void deleteHistory(Long id) {
        User user = getCurrentUser();
        if (user != null) {
            Optional<SearchHistory> history = historyRepository.findById(id);
            if (history.isPresent() && history.get().getUser().getId().equals(user.getId())) {
                historyRepository.delete(history.get());
            }
        }
    }

    @Override
    @Transactional
    public void clearAllHistory() {
        User user = getCurrentUser();
        if (user != null) {
            historyRepository.deleteByUserId(user.getId());
        }
    }
}