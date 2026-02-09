package com.hdtpt.pentachat.games.controller;

import com.hdtpt.pentachat.games.model.Game;
import com.hdtpt.pentachat.games.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games") // Đường dẫn API
public class GameController {

    @Autowired
    private GameService gameService;

    // API: Lấy danh sách game
    // Link gọi: GET http://localhost:8080/api/games
    @GetMapping
    public ResponseEntity<?> getAllGames() {
        List<Game> games = gameService.getGameList();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Lấy danh sách game thành công",
            "data", games
        ));
    }
}