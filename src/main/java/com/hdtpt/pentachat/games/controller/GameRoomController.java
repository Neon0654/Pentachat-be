package com.hdtpt.pentachat.games.controller;

import com.hdtpt.pentachat.model.Room;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/game")
public class GameRoomController {

    private final GameRoomService roomService;

    public GameRoomController(GameRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/start/{roomId}")
    public String startGame(@PathVariable Long roomId, Model model) {
        roomService.startGame(roomId);
        Room room = roomService.getRoom(roomId);
        model.addAttribute("room", room);
        return "game-status";
    }

    @GetMapping("/end/{roomId}")
    public String endGame(@PathVariable Long roomId, Model model) {
        roomService.endGame(roomId);
        Room room = roomService.getRoom(roomId);
        model.addAttribute("room", room);
        return "game-status";
    }
}
