package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.Game;
import com.hdtpt.pentachat.games.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GameService {
    
    @Autowired
    private GameRepository gameRepository;

    public List<Game> getGameList() {
        return gameRepository.findAll();
    }
    
    // --- CẬP NHẬT LẠI DANH SÁCH GAME NHẸ NHÀNG ---
    public void createDummyGames() {
        if (gameRepository.count() == 0) {
            // Game 1: Cờ Caro - Huyền thoại học đường
            gameRepository.save(new Game(null, "Cờ Caro (X/O)", "Đấu trí 5 nước thắng, vừa chat vừa đánh cờ.", "https://i.imgur.com/caro.png"));
            
            // Game 2: Rắn Săn Mồi - Đơn giản gây nghiện
            gameRepository.save(new Game(null, "Rắn Săn Mồi", "Điều khiển rắn ăn mồi, càng ăn càng dài.", "https://i.imgur.com/snake.png"));
            
            // Game 3: Đào Vàng - Giải trí nhẹ nhàng
            gameRepository.save(new Game(null, "Đào Vàng", "Thả ngàm kéo vàng, kim cương và túi bí ẩn.", "https://i.imgur.com/goldminer.png"));
            
            System.out.println("✅ Đã tạo danh sách Mini-Game cho Chat!");
        }
    }
}