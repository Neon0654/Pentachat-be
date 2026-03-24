package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.model.InviteStatus;
import com.hdtpt.pentachat.games.model.RoomInvite;
import com.hdtpt.pentachat.games.repository.RoomInviteRepository;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomInviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public RoomInvite inviteUser(Long roomId, Long inviterId, Long inviteeId) {
        validateInviteRequest(roomId, inviterId, inviteeId);

        RoomInvite existingInvite = inviteRepository.findByRoomIdAndInviteeId(roomId, inviteeId).orElse(null);
        if (existingInvite != null) {
            if (existingInvite.getStatus() == InviteStatus.PENDING) {
                throw new RuntimeException("Invite is already pending");
            }
            if (existingInvite.getStatus() == InviteStatus.ACCEPTED) {
                throw new RuntimeException("User is already in the room");
            }

            existingInvite.setInviterId(inviterId);
            existingInvite.setStatus(InviteStatus.PENDING);
            RoomInvite resentInvite = inviteRepository.save(existingInvite);
            messagingTemplate.convertAndSend("/topic/notifications." + inviteeId, resentInvite);
            messagingTemplate.convertAndSend("/topic/room." + roomId, "INVITE_SENT");
            log.info("Invite resent for room {} from {} to {}", roomId, inviterId, inviteeId);
            return resentInvite;
        }

        RoomInvite invite = RoomInvite.builder()
                .roomId(roomId)
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .status(InviteStatus.PENDING)
                .build();

        RoomInvite savedInvite = inviteRepository.save(invite);
        messagingTemplate.convertAndSend("/topic/notifications." + inviteeId, savedInvite);
        messagingTemplate.convertAndSend("/topic/room." + roomId, "INVITE_SENT");
        log.info("Invite saved for room {} from {} to {}", roomId, inviterId, inviteeId);
        return savedInvite;
    }

    public Map<String, Object> getRoomMembers(Long roomId) {
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        Map<String, Object> response = new HashMap<>();

        if (invites.isEmpty()) {
            return response;
        }

        Long ownerId = invites.get(0).getInviterId();
        String ownerName = userRepository.findById(ownerId).map(user -> user.getUsername()).orElse("Unknown");

        Set<Long> userIds = new HashSet<>();
        userIds.add(ownerId);
        invites.stream()
                .filter(invite -> invite.getStatus() == InviteStatus.ACCEPTED)
                .forEach(invite -> userIds.add(invite.getInviteeId()));

        List<String> memberNames = userRepository.findAllById(userIds).stream()
                .map(user -> user.getUsername())
                .collect(Collectors.toList());

        response.put("owner", ownerName);
        response.put("members", memberNames);
        return response;
    }

    public void acceptInvite(Long inviteId) {
        RoomInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay loi moi voi ID: " + inviteId));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        log.info("Invite {} accepted by user {} for room {}", inviteId, invite.getInviteeId(), invite.getRoomId());
    }

    public void leaveRoom(Long roomId, Long userId) {
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        if (invites.isEmpty()) {
            return;
        }

        Long ownerId = invites.get(0).getInviterId();
        if (ownerId.equals(userId)) {
            invites.forEach(inviteRepository::delete);
            log.info("Owner {} closed room {}", userId, roomId);
            return;
        }

        invites.stream()
                .filter(invite -> invite.getInviteeId().equals(userId))
                .forEach(inviteRepository::delete);
        log.info("User {} left room {}", userId, roomId);
    }

    private void validateInviteRequest(Long roomId, Long inviterId, Long inviteeId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }
        if (inviterId == null || inviteeId == null) {
            throw new RuntimeException("Inviter ID and invitee ID are required");
        }
        if (inviterId.equals(inviteeId)) {
            throw new RuntimeException("Cannot invite yourself");
        }
        if (userRepository.findById(inviterId).isEmpty()) {
            throw new RuntimeException("Inviter does not exist");
        }
        if (userRepository.findById(inviteeId).isEmpty()) {
            throw new RuntimeException("Invitee does not exist");
        }
    }
}
