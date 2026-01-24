package com.smartrental.backend.controller;

import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.Transaction;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.TransactionRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Import đúng kiểu dữ liệu tiền tệ
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;

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

        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại"));

        // 🟢 SỬA LỖI: Sử dụng walletBalance (BigDecimal) và so sánh bằng compareTo
        BigDecimal packagePrice = pkg.getPrice();

        if (user.getWalletBalance() == null || user.getWalletBalance().compareTo(packagePrice) < 0) {
            return ResponseEntity.badRequest().body("Số dư ví không đủ để đăng ký gói này!");
        }
        user.setMembershipPackage(pkg); // Gán gói VIP vừa mua
        user.setMembershipExpiresAt(LocalDateTime.now().plusDays(pkg.getDurationDays())); // Tính ngày hết hạn
        userRepository.save(user); // Lưu lại thông tin mới

        // 🟢 SỬA LỖI: Thực hiện trừ tiền ví
        user.setWalletBalance(user.getWalletBalance().subtract(packagePrice));
        userRepository.save(user);

        // 🟢 SỬA LỖI: Lưu lịch sử giao dịch khớp với Entity Transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(packagePrice.negate()) // Số tiền âm (chi ra)
                .type("PURCHASE_PACKAGE")
                .description("Đăng ký gói dịch vụ: " + pkg.getName())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return ResponseEntity.ok("Thanh toán thành công. Chúc bạn sớm tìm được khách thuê!");
    }
}