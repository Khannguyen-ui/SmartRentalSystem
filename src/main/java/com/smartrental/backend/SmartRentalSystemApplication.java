package com.smartrental.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartRentalSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRentalSystemApplication.class, args);
    }


    @Bean
    public CommandLineRunner fixData(JdbcTemplate jdbcTemplate) {
        return args -> {

            jdbcTemplate.update("UPDATE rooms SET current_tenants = 0 WHERE current_tenants IS NULL");


            jdbcTemplate.update("UPDATE rooms SET capacity = 1 WHERE capacity IS NULL");


            jdbcTemplate.update("UPDATE rooms SET rental_type = 'WHOLE' WHERE rental_type IS NULL");


        };
    }
}