package com.hdtpt.pentachat.controller;

import com.hdtpt.pentachat.model.Room;
import com.hdtpt.pentachat.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/game")
public class GameController {

    private final RoomService roomService;

    public GameController(RoomService roomService) {
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
