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

    // Inject Service Kiểm duyệt AI
    private final ContentModerationService moderationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public MarketItemDTO createItem(MarketItemDTO dto) {
        User seller = getCurrentUser();
        MarketItem item = marketMapper.toEntity(dto);
        item.setUser(seller);

        // --- CẬP NHẬT LOGIC: AI DUYỆT TỰ ĐỘNG ---
        // 1. Kiểm tra văn bản (Tiêu đề + Mô tả)
        boolean isTextSafe = moderationService.isTextSafe(dto.getTitle() + " " + dto.getDescription());

        // 2. Kiểm tra hình ảnh (Duyệt qua danh sách ảnh)
        boolean isImageSafe = true;
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (String imgUrl : dto.getImages()) {
                if (!moderationService.isImageSafe(imgUrl)) {
                    isImageSafe = false;
                    break;
                }
            }
        }

        // 3. Quyết định trạng thái
        if (isTextSafe && isImageSafe) {
            // An toàn -> Cho hiện luôn (đỡ tốn công Admin duyệt)
            item.setStatus("AVAILABLE");
        } else {
            // Vi phạm -> Chặn luôn (REJECTED) hoặc để PENDING chờ Admin xem lại
            // Ở đây mình set REJECTED để chặn hàng cấm ngay lập tức
            item.setStatus("REJECTED");
            System.out.println(">>> TIN ĐĂNG BỊ AI TỪ CHỐI DO VI PHẠM CHÍNH SÁCH");
        }
        // ----------------------------------------

        MarketItem savedItem = marketRepository.save(item);
        return marketMapper.toResponse(savedItem);
    }

    @Override
    public List<MarketItemDTO> getAllAvailableItems() {
        return marketRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE")
                .stream().map(marketMapper::toResponse).collect(Collectors.toList());
    }

    // --- (MỚI) DÀNH CHO ADMIN ---
    // Lấy danh sách cần duyệt (nếu bạn muốn quy trình thủ công hoặc xem lại tin bị AI chặn)
    public List<MarketItemDTO> getPendingItems() {
        return marketRepository.findByStatusOrderByCreatedAtDesc("PENDING") // Hoặc REJECTED nếu muốn review lại
                .stream().map(marketMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void verifyItem(Long itemId, boolean isApproved) {
        MarketItem item = marketRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món đồ không tồn tại"));

        if (isApproved) {
            item.setStatus("AVAILABLE");
        } else {
            item.setStatus("REJECTED");
        }
        marketRepository.save(item);
    }
    // ----------------------------

    @Override
    public List<MarketItemDTO> searchItems(String keyword) {
        return marketRepository.searchItems(keyword)
                .stream().map(marketMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsSold(Long itemId, Long buyerId) {
        User seller = getCurrentUser();
        MarketItem item = marketRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món đồ không tồn tại"));

        if (!item.getUser().getId().equals(seller.getId())) {
            throw new RuntimeException("Bạn không phải chủ món đồ này!");
        }

        if (buyerId != null) {
            User buyer = userRepository.findById(buyerId)
                    .orElseThrow(() -> new RuntimeException("Người mua không tồn tại"));

            if (buyer.getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không thể tự mua đồ của chính mình!");
            }
            item.setBuyer(buyer);
        }

        item.setStatus("SOLD");
        marketRepository.save(item);
    }
}