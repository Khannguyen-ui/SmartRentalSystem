package com.smartrental.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class SmartRentalSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRentalSystemApplication.class, args);
    }

    // --- THÊM ĐOẠN NÀY VÀO ---
    @Bean
    public CommandLineRunner fixData(JdbcTemplate jdbcTemplate) {
        return args -> {
            // Fix 1: Lấp đầy current_tenants = 0 nếu null
            jdbcTemplate.update("UPDATE rooms SET current_tenants = 0 WHERE current_tenants IS NULL");

            // Fix 2: Lấp đầy capacity = 1 (mặc định) nếu null
            jdbcTemplate.update("UPDATE rooms SET capacity = 1 WHERE capacity IS NULL");

            // Fix 3: Lấp đầy rental_type = 'WHOLE' (mặc định) nếu null
            jdbcTemplate.update("UPDATE rooms SET rental_type = 'WHOLE' WHERE rental_type IS NULL");

            System.out.println("✅ ĐÃ FIX TOÀN BỘ DỮ LIỆU CŨ THÀNH CÔNG!");
        };
    }
}