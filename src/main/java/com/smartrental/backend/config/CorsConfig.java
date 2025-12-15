package com.smartrental.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Cho phép tất cả các đường dẫn API
                .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Cho phép ReactJS (3000) và Vite (5173) truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các method được phép
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}