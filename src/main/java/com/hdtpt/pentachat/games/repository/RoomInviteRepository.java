package com.hdtpt.pentachat.games.repository;

import com.hdtpt.pentachat.games.model.InviteStatus;
import com.hdtpt.pentachat.games.model.RoomInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomInviteRepository extends JpaRepository<RoomInvite, Long> {
    // Tìm danh sách lời mời đang chờ (PENDING) của một người cụ thể
    List<RoomInvite> findByInviteeIdAndStatus(Long inviteeId, String status);

    List<RoomInvite> findByRoomIdAndStatus(Long roomId, InviteStatus status);

    List<RoomInvite> findByRoomId(Long roomId);
}