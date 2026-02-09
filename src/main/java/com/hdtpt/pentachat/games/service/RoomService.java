package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.InviteStatus;
import com.hdtpt.pentachat.games.model.RoomInvite;
import com.hdtpt.pentachat.games.repository.RoomInviteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomInviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.hdtpt.pentachat.identity.repository.UserRepository userRepository;

    /**
     * Hàm mời người chơi vào phòng
     */
    public RoomInvite inviteUser(Long roomId, Long inviterId, Long inviteeId) {
        // 1. Tạo đối tượng RoomInvite
        RoomInvite invite = RoomInvite.builder()
                .roomId(roomId)
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .status(InviteStatus.PENDING)
                .build();

        // 2. Lưu vào SQL Server qua Repository
        RoomInvite savedInvite = inviteRepository.save(invite);
        log.info("🎮 Đã lưu lời mời vào phòng {} từ {} tới {}", roomId, inviterId, inviteeId);

        // 3. Gửi thông báo Real-time cho người được mời qua WebSocket
        // Người nhận sẽ subscribe kênh: /user/{inviteeId}/queue/notifications
        messagingTemplate.convertAndSend("/topic/notifications." + inviteeId, savedInvite);
        messagingTemplate.convertAndSend("/topic/room." + roomId, "INVITE_SENT");
        log.info("🎮 Đã bắn thông báo tới kênh: /topic/notifications.{}", inviteeId);
        return savedInvite;
    }

    public Map<String, Object> getRoomMembers(Long roomId) {
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        Map<String, Object> response = new java.util.HashMap<>();

        if (invites.isEmpty())
            return response;

        // 1. Chủ phòng là người tạo lời mời đầu tiên
        Long ownerId = invites.get(0).getInviterId();
        String ownerName = userRepository.findById(ownerId).map(u -> u.getUsername()).orElse("Unknown");

        // 2. Danh sách những người đã vào (gồm cả chủ phòng)
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        userIds.add(ownerId);
        invites.stream()
                .filter(inv -> inv.getStatus() == InviteStatus.ACCEPTED)
                .forEach(inv -> userIds.add(inv.getInviteeId()));

        List<String> memberNames = userRepository.findAllById(userIds).stream()
                .map(u -> u.getUsername())
                .collect(Collectors.toList());

        response.put("owner", ownerName); // Tên chủ phòng thật sự
        response.put("members", memberNames);
        return response;
    }

    public void acceptInvite(Long inviteId) {
        // 1. Tìm lời mời trong DB theo ID
        RoomInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời với ID: " + inviteId));

        // 2. Cập nhật trạng thái thành ACCEPTED
        invite.setStatus(InviteStatus.ACCEPTED);

        // 3. Lưu lại vào SQL Server
        inviteRepository.save(invite);

        log.info("✅ Lời mời {} đã được chấp nhận. Người dùng {} đã vào phòng {}",
                inviteId, invite.getInviteeId(), invite.getRoomId());
    }

    public void leaveRoom(Long roomId, Long userId) {
        // Tìm lời mời của người này trong phòng và xóa đi
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        invites.stream()
                .filter(inv -> inv.getInviteeId().equals(userId) || inv.getInviterId().equals(userId))
                .forEach(inv -> inviteRepository.delete(inv));
    }
}