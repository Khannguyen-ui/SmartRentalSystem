package com.smartrental.backend.seeder;

import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Admin")
                    .role(User.Role.ADMIN)   // ✅ ĐÚNG ENUM
                    .isActive(true)          // ✅ ĐÚNG FIELD
                    .build();

            userRepository.save(admin);
            System.out.println("✅ ADMIN ACCOUNT CREATED");
        } else {
            System.out.println("ℹ️ ADMIN ACCOUNT ALREADY EXISTS");
        }
    }
}
