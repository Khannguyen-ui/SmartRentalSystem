package com.smartrental.backend.service.impl;

import com.smartrental.backend.entity.*;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.NotificationService;
import com.smartrental.backend.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicePackageServiceImpl implements ServicePackageService {

    private final UserRepository userRepository;
    private final ServicePackageRepository packageRepository; // Bạn cần tạo Repository này
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void buyMembership(Long userId, Long packageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ServicePackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Gói dịch vụ không tồn tại"));

        // Kiểm tra số dư ví
        if (user.getWalletBalance().compareTo(pkg.getPrice()) < 0) {
            throw new RuntimeException("Số dư ví không đủ để nâng cấp gói");
        }

        // 1. Trừ tiền ví và cập nhật hạng hội viên
        user.setWalletBalance(user.getWalletBalance().subtract(pkg.getPrice()));

        // Giả sử bạn có trường membershipPackage trong User entity như đã bàn ở trên
        user.setMembershipPackage(pkg);
        user.setMembershipExpiresAt(LocalDateTime.now().plusDays(pkg.getDurationDays()));

        userRepository.save(user);

        // 2. Lưu lịch sử giao dịch (Dùng Builder cho đồng bộ với dự án của bạn)
        Transaction trans = Transaction.builder()
                .user(user)
                .amount(pkg.getPrice())
                .type("BUY_MEMBERSHIP")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(trans);

        // 3. Gửi thông báo real-time
        notificationService.sendNotification(
                user,
                "Nâng cấp hội viên thành công",
                "Chúc mừng! Bạn đã trở thành hội viên " + pkg.getName() + ". Thời hạn: " + pkg.getDurationDays() + " ngày.",
                NotificationType.SYSTEM,
                trans.getId()
        );
    }
}