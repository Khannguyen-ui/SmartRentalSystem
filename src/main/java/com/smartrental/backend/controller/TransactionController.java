package com.smartrental.backend.controller;

import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.Transaction;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.TransactionRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final NotificationService notificationService;

    @GetMapping("/my-history")
    public ResponseEntity<List<Transaction>> getMyHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @PostMapping("/purchase-package")
    @Transactional
    public ResponseEntity<?> purchasePackage(@RequestBody Long packageId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // 🟢 MỚI: CHẶN ĐĂNG KÝ KHI CÒN HẠN GÓI CŨ
        if (user.getMembershipPackage() != null &&
                user.getMembershipExpiresAt() != null &&
                user.getMembershipExpiresAt().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Bạn đang có một gói dịch vụ còn hiệu lực. Không thể đăng ký thêm!");
        }

        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại"));

        BigDecimal packagePrice = pkg.getPrice();

        if (user.getWalletBalance() == null || user.getWalletBalance().compareTo(packagePrice) < 0) {
            return ResponseEntity.badRequest().body("Số dư ví không đủ để đăng ký gói này!");
        }

        user.setMembershipPackage(pkg);
        user.setMembershipExpiresAt(LocalDateTime.now().plusDays(pkg.getDurationDays()));

        // Trừ tiền ví
        user.setWalletBalance(user.getWalletBalance().subtract(packagePrice));
        userRepository.save(user);

        // Lưu lịch sử giao dịch
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(packagePrice.negate())
                .type("PURCHASE_PACKAGE")
                .description("Đăng ký gói dịch vụ: " + pkg.getName())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        notificationService.sendNotification(
                user,
                "Đăng ký gói VIP thành công",
                "Bạn đã thanh toán thành công gói " + pkg.getName() + ". Số tiền: -" + packagePrice.toPlainString() + " VNĐ",
                NotificationType.PURCHASE_PACKAGE,
                transaction.getId()
        );

        return ResponseEntity.ok("Thanh toán thành công. Chúc bạn sớm tìm được khách thuê!");
    }
}