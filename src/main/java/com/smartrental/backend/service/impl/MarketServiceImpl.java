package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.MarketItemDTO;
import com.smartrental.backend.entity.MarketItem;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.MarketMapper;
import com.smartrental.backend.repository.MarketItemRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final MarketItemRepository marketRepository;
    private final UserRepository userRepository;
    private final MarketMapper marketMapper;

    // Helper: Lấy user đang đăng nhập từ Token
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public MarketItemDTO createItem(MarketItemDTO dto) {
        User seller = getCurrentUser();

        // Convert DTO -> Entity
        MarketItem item = marketMapper.toEntity(dto);
        item.setUser(seller); // Gán người bán là user đang login
        item.setStatus("AVAILABLE"); // Mặc định là đang bán

        MarketItem savedItem = marketRepository.save(item);

        return marketMapper.toResponse(savedItem);
    }

    @Override
    public List<MarketItemDTO> getAllAvailableItems() {
        // Lấy tất cả đồ có status là AVAILABLE, sắp xếp mới nhất
        return marketRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE")
                .stream()
                .map(marketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarketItemDTO> searchItems(String keyword) {
        // Tìm kiếm theo từ khóa (Title hoặc Description)
        return marketRepository.searchItems(keyword)
                .stream()
                .map(marketMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- CẬP NHẬT LOGIC: CHỐT ĐƠN CÓ NGƯỜI MUA ---
    @Override
    @Transactional
    public void markAsSold(Long itemId, Long buyerId) {
        User seller = getCurrentUser();

        MarketItem item = marketRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món đồ không tồn tại"));

        // 1. Kiểm tra quyền sở hữu: Chỉ người đăng mới được sửa trạng thái
        if (!item.getUser().getId().equals(seller.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa món đồ này!");
        }

        // 2. Cập nhật người mua (Nếu có)
        if (buyerId != null) {
            User buyer = userRepository.findById(buyerId)
                    .orElseThrow(() -> new RuntimeException("Người mua không tồn tại"));

            // Không cho phép tự mua đồ của chính mình
            if (buyer.getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không thể tự mua đồ của chính mình!");
            }

            item.setBuyer(buyer);
        }

        // 3. Đổi trạng thái
        item.setStatus("SOLD");
        marketRepository.save(item);
    }
}