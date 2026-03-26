package com.hdtpt.pentachat.games.model;

import com.hdtpt.pentachat.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Phòng chơi Poker.
 */
@Entity
@Table(name = "poker_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomCode;

    @Column(nullable = false)
    private String status; // WAITING (Chờ), PLAYING (Đang chơi), ENDED (Kết thúc)

    private int maxPlayers;

    private Double minBet;

    @Column(name = "host_id")
    private Long hostId;
}
