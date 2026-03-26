package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.Room;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameRoomService {

    // Gia lap bang rooms tam thoi cho luong view controller.
    private final Map<Long, Room> rooms = new HashMap<>();

    public GameRoomService() {
        rooms.put(1L, Room.builder()
                .id(1L)
                .roomCode("ROOM-1")
                .status("WAITING")
                .build());
        rooms.put(2L, Room.builder()
                .id(2L)
                .roomCode("ROOM-2")
                .status("WAITING")
                .build());
    }

    public void startGame(Long roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room khong ton tai");
        }

        if (!"WAITING".equals(room.getStatus())) {
            throw new RuntimeException("Khong the start game. Trang thai hien tai: " + room.getStatus());
        }

        room.setStatus("PLAYING");
    }

    public void endGame(Long roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room khong ton tai");
        }

        if (!"PLAYING".equals(room.getStatus())) {
            throw new RuntimeException("Khong the end game. Trang thai hien tai: " + room.getStatus());
        }

        room.setStatus("ENDED");
    }

    public Room getRoom(Long roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room khong ton tai");
        }
        return room;
    }
}
