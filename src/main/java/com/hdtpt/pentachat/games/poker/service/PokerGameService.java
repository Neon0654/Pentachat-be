package com.hdtpt.pentachat.games.poker.service;

import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.games.model.Room;
import com.hdtpt.pentachat.games.poker.dto.*;
import com.hdtpt.pentachat.games.poker.model.*;
import com.hdtpt.pentachat.games.poker.repository.RoundHistoryRepository;
import com.hdtpt.pentachat.games.poker.util.PokerHandEvaluator;
import com.hdtpt.pentachat.games.repository.RoomRepository;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PokerGameService {

    private final GameSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final WalletService walletService;
    private final RoundHistoryRepository roundHistoryRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final Map<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * Xử lý hành động từ người chơi.
     */
    public void handleAction(Long userId, PokerActionDTO action) {
        String roomId = action.getRoomId();
        ReentrantLock lock = roomLocks.computeIfAbsent(roomId, k -> new ReentrantLock());
        lock.lock();
        try {
            GameSession session = sessionManager.getSession(roomId);
            if (session == null)
                return;

            // Xử lý bắt đầu game
            if (action.getType() == PokerActionDTO.ActionType.START) {
                if (session.getHostId().equals(userId) &&
                        (session.getPhase() == GamePhase.WAITING || session.getPhase() == GamePhase.SHOWDOWN
                                || session.getPhase() == GamePhase.ENDED)) {
                    startNewRound(session);
                }
                return;
            }

            // Kiểm tra xem có đúng lượt không
            if (session.getCurrentTurnUserId() == null ||
                    !session.getCurrentTurnUserId().equals(userId)) {
                return;
            }

            // Hủy timer hiện tại
            if (session.getTurnTimer() != null) {
                session.getTurnTimer().cancel(false);
            }

            processPlayerTurn(session, userId, action);

            // Broadcast trạng thái mới

        } finally {
            lock.unlock();
        }
    }

    private void startNewRound(GameSession session) {
        List<Long> activePlayers = new ArrayList<>(session.getSeatOrder());

        if (activePlayers.size() < 2) {
            session.setLastActionLog("Cần ít nhất 2 người chơi để bắt đầu.");
            broadcastState(session);
            return;
        }

        // Khởi tạo bộ bài mới
        session.setDeck(generateNewDeck());
        Collections.shuffle(session.getDeck());

        session.setPot(0.0);
        session.setCurrentRoundBet(0.0);
        session.getCommunityCards().clear();
        session.getHoleCards().clear();
        session.getPlayerBets().clear();
        session.getPlayersActedThisRound().clear();

        // Trừ tiền mù (Blinds) - Đơn giản hóa: mọi người đặt cược tối thiểu của phòng
        Double minBet = roomRepository.findByRoomCode(session.getRoomId()).get().getMinBet();
        for (Long pid : activePlayers) {
            session.getPlayerStatus().put(pid, PlayerStateDTO.PlayerStatus.ACTIVE);
            session.getPlayerBets().put(pid, minBet);
            session.getPlayerChips().put(pid, session.getPlayerChips().get(pid) - minBet);
            session.setPot(session.getPot() + minBet);
        }
        session.setCurrentRoundBet(minBet);

        // Chia bài tẩy
        for (Long pid : activePlayers) {
            List<String> cards = new ArrayList<>();
            cards.add(session.getDeck().remove(0));
            cards.add(session.getDeck().remove(0));
            session.getHoleCards().put(pid, cards);

            // Gửi bài riêng cho từng người
            messagingTemplate.convertAndSendToUser(
                    pid.toString(),
                    "/queue/poker/private",
                    new PrivateHandDTO(cards));

            // Fallback theo topic room+user để FE luôn nhận được bài tẩy
            messagingTemplate.convertAndSend(
                    "/topic/poker/" + session.getRoomId() + "/private/" + pid,
                    new PrivateHandDTO(cards));
        }

        session.setPhase(GamePhase.PRE_FLOP);
        session.setCurrentTurnUserId(activePlayers.get(0));
        session.setLastActionLog("Ván mới bắt đầu! Đang ở vòng Pre-Flop.");

        startTurnTimer(session);
        broadcastState(session);
    }

    private void processPlayerTurn(GameSession session, Long userId, PokerActionDTO action) {
        double currentBetForUser = session.getPlayerBets().getOrDefault(userId, 0.0);
        double amountToCall = session.getCurrentRoundBet() - currentBetForUser;
        double chips = session.getPlayerChips().get(userId);

        String username = userRepository.findById(userId)
                .map(User::getUsername).orElse("Ẩn danh");

        switch (action.getType()) {

            case FOLD:
                session.getPlayerStatus().put(userId, PlayerStateDTO.PlayerStatus.FOLDED);
                session.setLastActionLog(username + " đã bỏ bài.");
                break;

            case CHECK:
                if (amountToCall > 0) {
                    session.setLastActionLog(username + " không thể CHECK.");
                    startTurnTimer(session);
                    broadcastState(session);
                    return;
                }
                session.setLastActionLog(username + " CHECK.");
                break;

            case CALL:
                if (chips < amountToCall) {
                    session.setLastActionLog("Không đủ chip để CALL.");
                    startTurnTimer(session);
                    broadcastState(session);
                    return;
                }
                session.getPlayerChips().put(userId, chips - amountToCall);
                session.getPlayerBets().put(userId, session.getCurrentRoundBet());
                session.setPot(session.getPot() + amountToCall);
                session.setLastActionLog(username + " CALL.");
                break;

            case RAISE:
                double raiseAmount = action.getAmount();

                if (raiseAmount <= amountToCall) {
                    session.setLastActionLog("Raise phải lớn hơn CALL.");
                    startTurnTimer(session);
                    broadcastState(session);
                    return;
                }

                if (chips < raiseAmount) {
                    session.setLastActionLog("Không đủ chip để RAISE.");
                    startTurnTimer(session);
                    broadcastState(session);
                    return;
                }

                session.getPlayerChips().put(userId, chips - raiseAmount);

                double newBet = currentBetForUser + raiseAmount;
                session.getPlayerBets().put(userId, newBet);
                session.setCurrentRoundBet(newBet);
                session.setPot(session.getPot() + raiseAmount);

                session.setLastActionLog(username + " RAISE " + raiseAmount);
                break;

            default:
                return;
        }

        session.getPlayersActedThisRound().add(userId);
        moveToNextTurn(session);
    }

    private void moveToNextTurn(GameSession session) {

        List<Long> activePlayers = session.getSeatOrder().stream()
                .filter(id -> session.getPlayerStatus().get(id) == PlayerStateDTO.PlayerStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activePlayers.size() <= 1) {
            endRound(session);
            broadcastState(session);
            return;
        }

        boolean everyoneCalled = activePlayers.stream()
                .allMatch(id -> Math.abs(session.getPlayerBets().getOrDefault(id, 0.0)
                        - session.getCurrentRoundBet()) < 0.0001);

        boolean everyoneActed = session.getPlayersActedThisRound().containsAll(activePlayers);

        if (everyoneCalled && everyoneActed) {
            nextPhase(session);
            return;
        }

        int currentIndex = activePlayers.indexOf(session.getCurrentTurnUserId());
        int nextIndex = (currentIndex + 1) % activePlayers.size();
        Long nextUser = activePlayers.get(nextIndex);

        session.setCurrentTurnUserId(nextUser);
        startTurnTimer(session);
        broadcastState(session);
    }

    private void nextPhase(GameSession session) {

        session.setCurrentRoundBet(0.0);
        session.getPlayerBets().replaceAll((k, v) -> 0.0);
        session.getPlayersActedThisRound().clear();

        // 🔥 reset vòng
        switch (session.getPhase()) {
            case PRE_FLOP:
                dealCommunityCards(session, 3);
                session.setPhase(GamePhase.FLOP);
                break;
            case FLOP:
                dealCommunityCards(session, 1);
                session.setPhase(GamePhase.TURN);
                break;
            case TURN:
                dealCommunityCards(session, 1);
                session.setPhase(GamePhase.RIVER);
                break;
            case RIVER:
                session.setPhase(GamePhase.SHOWDOWN);
                determineWinner(session);
                broadcastState(session);
                return;
        }

        Long firstActive = session.getSeatOrder().stream()
                .filter(id -> session.getPlayerStatus().get(id) == PlayerStateDTO.PlayerStatus.ACTIVE)
                .findFirst().orElse(null);

        session.setCurrentTurnUserId(firstActive);
        startTurnTimer(session);
        broadcastState(session);
    }

    private void dealCommunityCards(GameSession session, int count) {
        for (int i = 0; i < count; i++) {
            session.getCommunityCards().add(session.getDeck().remove(0));
        }
    }

    private void determineWinner(GameSession session) {
        List<Long> activePlayers = session.getSeatOrder().stream()
                .filter(id -> session.getPlayerStatus().get(id) == PlayerStateDTO.PlayerStatus.ACTIVE)
                .collect(Collectors.toList());

        Long winnerId = null;
        PokerHandEvaluator.HandScore bestScore = null;
        String bestDescription = "";

        for (Long pid : activePlayers) {
            List<String> allCards = new ArrayList<>(session.getCommunityCards());
            allCards.addAll(session.getHoleCards().get(pid));
            PokerHandEvaluator.HandScore score = PokerHandEvaluator.evaluate(allCards);

            if (bestScore == null || score.compareTo(bestScore) > 0) {
                bestScore = score;
                winnerId = pid;
                bestDescription = score.getRank().getDescription();
            }
        }

        distributeWinnings(session, winnerId, bestDescription);
    }

    private void distributeWinnings(GameSession session, Long winnerId, String description) {
        String winnerName = userRepository.findById(winnerId).map(User::getUsername).orElse("Người thắng");
        session.setLastActionLog(winnerName + " đã thắng hũ " + session.getPot() + " với bộ " + description + "!");

        // Cập nhật ví (chỉ cộng tiền thắng vào memory chips trước)
        session.getPlayerChips().put(winnerId, session.getPlayerChips().get(winnerId) + session.getPot());
        // Lưu lịch sử
        RoundHistory history = RoundHistory.builder()
                .roomId(roomRepository.findByRoomCode(session.getRoomId()).get().getId())
                .winnerId(winnerId)
                .pot(session.getPot())
                .communityCards(String.join(",", session.getCommunityCards()))
                .handDescription(description)
                .build();
        roundHistoryRepository.save(history);

        // Kết thúc ván, cho phép host bắt đầu ván mới
        session.setPhase(GamePhase.SHOWDOWN);
        session.setCurrentTurnUserId(null);
        if (session.getTurnTimer() != null)
            session.getTurnTimer().cancel(false);
    }

    private void endRound(GameSession session) {
        // Trường hợp chỉ còn 1 người chưa Fold
        Long winnerId = session.getSeatOrder().stream()
                .filter(id -> session.getPlayerStatus().get(id) == PlayerStateDTO.PlayerStatus.ACTIVE)
                .findFirst().orElse(null);

        if (winnerId != null) {
            distributeWinnings(session, winnerId, "Mọi người đã bỏ bài");
        }
    }

    private List<String> generateNewDeck() {
        String[] ranks = { "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A" };
        String[] suits = { "S", "H", "D", "C" };
        List<String> deck = new ArrayList<>();
        for (String s : suits) {
            for (String r : ranks) {
                deck.add(r + s);
            }
        }
        return deck;
    }

    private void startTurnTimer(GameSession session) {
        if (session.getTurnTimer() != null)
            session.getTurnTimer().cancel(false);

        String roomId = session.getRoomId();
        Long userId = session.getCurrentTurnUserId();

        session.setTurnTimer(scheduler.schedule(() -> {
            if (!session.getCurrentTurnUserId().equals(userId))
                return;

            handleAction(userId,
                    new PokerActionDTO(PokerActionDTO.ActionType.FOLD, 0.0, roomId));

        }, 30, TimeUnit.SECONDS));
    }

    public GameStateDTO getGameState(GameSession session) {
        List<PlayerStateDTO> players = session.getSeatOrder().stream().map(userId -> {
            User user = userRepository.findById(userId).orElse(null);
            boolean isHost = userId.equals(session.getHostId());
            System.out.println(String.format("🔥 PokerGameService.getGameState: userId=%d, hostId=%s, isHost=%s, username=%s", 
                userId, session.getHostId(), isHost, user != null ? user.getUsername() : "null"));
            return PlayerStateDTO.builder()
                    .userId(userId)
                    .username(user != null ? user.getUsername() : "Unknown Name")
                    .seatIndex(session.getSeatOrder().indexOf(userId))
                    .chips(session.getPlayerChips().get(userId))
                    .status(session.getPlayerStatus().get(userId))
                    .betThisRound(session.getPlayerBets().getOrDefault(userId, 0.0))
                    .isHost(isHost)
                    .build();
        }).collect(Collectors.toList());

        return GameStateDTO.builder()
                .roomId(session.getRoomId())
                .pot(session.getPot())
                .currentRoundBet(session.getCurrentRoundBet())
                .currentTurnUserId(session.getCurrentTurnUserId())
                .phase(session.getPhase())
                .communityCards(session.getCommunityCards())
                .players(players)
                .log(session.getLastActionLog())
                .lastActionLog(session.getLastActionLog())
                .build();
    }

    public void broadcastState(GameSession session) {
        GameStateDTO state = getGameState(session);
        messagingTemplate.convertAndSend("/topic/poker/" + session.getRoomId(), state);
    }
}
