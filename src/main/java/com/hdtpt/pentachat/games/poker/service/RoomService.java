package com.hdtpt.pentachat.games.poker.service;

import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.games.model.Room;
import com.hdtpt.pentachat.games.poker.dto.PlayerStateDTO;
import com.hdtpt.pentachat.games.poker.model.GamePhase;
import com.hdtpt.pentachat.games.poker.model.GameSession;
import com.hdtpt.pentachat.games.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ quản lý phòng chơi Poker.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final GameSessionManager sessionManager;
    private final WalletService walletService;
    private final PokerGameService gameService;

    @Transactional
    public Room createRoom(Long hostId, int maxPlayers, Double minBet) {
        String roomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println(String.format("🔥 RoomService.createRoom: hostId=%d, roomCode=%s", hostId, roomCode));
        Room room = Room.builder()
                .roomCode(roomCode)
                .hostId(hostId)
                .maxPlayers(maxPlayers)
                .minBet(minBet)
                .status("WAITING")
                .build();
        
        room = roomRepository.save(room);
        sessionManager.createSession(roomCode, hostId);
        
        // Verify hostId was set
        GameSession verifySession = sessionManager.getSession(roomCode);
        System.out.println(String.format("🔥 RoomService.createRoom: GameSession hostId after creation=%s", 
            verifySession != null ? verifySession.getHostId() : "null"));
        
        // Tự động cho host vào phòng (trong RAM)
        joinRoomInternal(roomCode, hostId);
        
        return room;
    }

    public List<Room> listOpenRooms() {
        return roomRepository.findAll();
    }

    public void joinRoomInternal(String roomCode, Long userId) {
        GameSession session = sessionManager.getSession(roomCode);
        if (session == null) throw new AppException("Phòng không tồn tại.");
        
        if (session.getSeatOrder().contains(userId)) return;
        
        Room room = roomRepository.findByRoomCode(roomCode).orElse(null);
        if (room != null && session.getSeatOrder().size() >= room.getMaxPlayers()) {
            throw new AppException("Phòng đã đầy.");
        }

        // Kiểm tra tiền của user
        Double balance = walletService.getBalance(userId).getBalance();
        if (balance < room.getMinBet() * 10) { // Cần ít nhất 10 lần mức cược tối thiểu
            throw new AppException("Bạn không có đủ tiền để vào phòng này (tối thiểu " + (room.getMinBet() * 10) + ").");
        }

        session.getSeatOrder().add(userId);
        session.getPlayerChips().put(userId, balance); // Đưa toàn bộ tiền (hoặc một phần tùy logic) vào bàn
        session.getPlayerStatus().put(userId, PlayerStateDTO.PlayerStatus.WAITING);
        session.getPlayerBets().put(userId, 0.0);
        
        session.setLastActionLog("Người chơi mới đã tham gia.");
        gameService.broadcastState(session);
    }

    @Transactional
    public void leaveRoom(String roomCode, Long userId) {
        GameSession session = sessionManager.getSession(roomCode);
        if (session != null) {
            session.getSeatOrder().remove(userId);
            session.getPlayerStatus().remove(userId);
            session.getPlayerChips().remove(userId);
            session.getPlayerBets().remove(userId);
            
            if (session.getSeatOrder().isEmpty()) {
                sessionManager.removeSession(roomCode);
                roomRepository.findByRoomCode(roomCode).ifPresent(roomRepository::delete);
            } else {
                if (userId.equals(session.getHostId())) {
                    session.setHostId(session.getSeatOrder().get(0));
                }
                session.setLastActionLog("Một người chơi đã rời phòng.");
                gameService.broadcastState(session);
            }
        }
    }
}
