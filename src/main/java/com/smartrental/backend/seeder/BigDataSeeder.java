package com.smartrental.backend.seeder; // [cite: 41]

import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BigDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Chỉ chạy nếu DB chưa có nhiều dữ liệu (tránh chạy trùng lặp mỗi lần khởi động)
        if (userRepository.count() < 100) {
            System.out.println("⏳ Đang tạo dữ liệu mẫu số lượng lớn (Big Data)...");
            long startTime = System.currentTimeMillis();

            List<User> userBatch = new ArrayList<>();
            String encodedPassword = passwordEncoder.encode("123456"); // Mật khẩu chung cho tất cả

            // Tạo 10.000 user
            for (int i = 1; i <= 10000; i++) {
                User user = User.builder()
                        .email("testuser" + i + "@example.com") // Email không trùng
                        .fullName("User Test " + i)
                        .phone("09" + String.format("%08d", i)) // Số điện thoại giả
                        .password(encodedPassword)
                        .role(User.Role.TENANT) //
                        .walletBalance(BigDecimal.valueOf(1000000)) // Ví có sẵn 1 triệu
                        .kycStatus("UNVERIFIED")
                        .isActive(true)
                        .build();

                userBatch.add(user);

                // Cứ mỗi 1000 user thì lưu vào DB một lần để đỡ tốn RAM (Batch Insert)
                if (i % 1000 == 0) {
                    userRepository.saveAll(userBatch);
                    userBatch.clear();
                    System.out.println("--> Đã lưu " + i + " users...");
                }
            }

            // Lưu nốt số còn lại nếu có
            if (!userBatch.isEmpty()) {
                userRepository.saveAll(userBatch);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("✅ HOÀN TẤT! Đã thêm 10.000 user trong " + (endTime - startTime) + "ms");
        }
    }
}