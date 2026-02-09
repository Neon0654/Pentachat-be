package com.hdtpt.pentachat;

import com.hdtpt.pentachat.games.service.GameService; // <-- Import Service
import org.springframework.boot.CommandLineRunner;     // <-- Import Runner
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;    // <-- Import Bean

@SpringBootApplication
public class ProjectGaugeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectGaugeApplication.class, args);
    }

    // --- ĐOẠN CODE TỰ ĐỘNG TẠO GAME MINI ---
    @Bean
    CommandLineRunner initData(GameService gameService) {
        return args -> {
            gameService.createDummyGames(); // Tự động tạo Cờ Caro, Rắn săn mồi...
        };
    }
}