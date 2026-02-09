package com.hdtpt.pentachat.games.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ lời mời vào phòng chơi game
 */
@Entity
@Table(name = "room_invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInvite {

    @Id
    private String id;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String inviterId; // ID người gửi lời mời

    @Column(nullable = false)
    private String inviteeId; // ID người nhận lời mời

    @Enumerated(EnumType.STRING)
    private InviteStatus status; // Trạng thái: PENDING, ACCEPTED, REJECTED

    private LocalDateTime createdAt;
}

