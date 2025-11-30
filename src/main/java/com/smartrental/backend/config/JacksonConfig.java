package com.smartrental.backend.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernateModule() {
        Hibernate6Module module = new Hibernate6Module();
        // Cấu hình này giúp Jackson bỏ qua (trả về null) các field Lazy chưa được load
        // thay vì cố gắng load chúng và gây lỗi hoặc vòng lặp vô tận.
        module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}