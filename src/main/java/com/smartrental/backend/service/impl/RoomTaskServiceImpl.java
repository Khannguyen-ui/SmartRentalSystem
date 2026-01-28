package com.smartrental.backend.service.impl;

import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.ServicePackageRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.NotificationService;
import com.smartrental.backend.service.RoomTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomTaskServiceImpl implements RoomTaskService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ServicePackageRepository servicePackageRepository;

    // 🟢 TỰ ĐỘNG 1: Nhắc nhở trước 24h (8h sáng hàng ngày)
    @Override
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void checkAndNotifyExpiringRooms() {
        log.info("Automation: Đang quét tin đăng sắp hết hạn...");
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        List<Room> expiringSoon = roomRepository.findExpiringSoon(tomorrow);

        for (Room room : expiringSoon) {
            notificationService.sendNotification(
                    room.getLandlord(),
                    "Tin đăng sắp hết hạn",
                    "Tin '" + room.getTitle() + "' sẽ hết hạn vào ngày mai. Hãy kiểm tra số dư để tự động gia hạn hoặc đẩy tin thủ công!",
                    NotificationType.ROOM_EXPIRING,
                    room.getId()
            );
        }
    }

    // 🟢 TỰ ĐỘNG 2: Xử lý Gia hạn hoặc Ẩn tin (0h đêm hàng ngày)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoDeactivateExpiredRooms() {
        log.info("Automation: Bắt đầu xử lý tin hết hạn và tự động gia hạn...");
        LocalDateTime now = LocalDateTime.now();

        List<Room> expiredRooms = roomRepository.findAllExpiredActiveRooms(now);

        for (Room room : expiredRooms) {
            User landlord = room.getLandlord();

            // 🟢 SỬA TẠI ĐÂY: Sử dụng getAutoRenew() và kiểm tra null
            if (room.getAutoRenew() != null && room.getAutoRenew()) {
                ServicePackage pkg = servicePackageRepository.findById(room.getServicePackageId()).orElse(null);

                if (pkg != null && landlord.getWalletBalance() != null &&
                        landlord.getWalletBalance().compareTo(pkg.getPrice()) >= 0) {

                    // Logic gia hạn...
                    landlord.setWalletBalance(landlord.getWalletBalance().subtract(pkg.getPrice()));
                    userRepository.save(landlord);

                    room.setExpirationDate(now.plusDays(pkg.getDurationDays()));
                    room.setLastPushedAt(now);
                    room.setStatus(Room.Status.ACTIVE);
                    roomRepository.save(room);

                    notificationService.sendNotification(
                            landlord, "Tự động gia hạn thành công",
                            "Tin '" + room.getTitle() + "' đã được gia hạn thành công.",
                            NotificationType.PURCHASE_PACKAGE, room.getId()
                    );
                    continue;
                } else {
                    // Thông báo hết tiền...
                    notificationService.sendNotification(
                            landlord, "Tự động gia hạn thất bại",
                            "Số dư không đủ để gia hạn tin '" + room.getTitle() + "'. Tin đã bị ẩn.",
                            NotificationType.SYSTEM, room.getId()
                    );
                }
            }

            // Ẩn tin nếu không có auto-renew hoặc renew thất bại
            room.setStatus(Room.Status.EXPIRED);
            roomRepository.save(room);

            notificationService.sendNotification(
                    landlord, "Tin đăng đã hết hạn",
                    "Tin '" + room.getTitle() + "' đã hết hạn hiển thị và hiện đã bị ẩn.",
                    NotificationType.SYSTEM, room.getId()
            );
        }
    }
}