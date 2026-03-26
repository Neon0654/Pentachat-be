package com.hdtpt.pentachat.games.poker.model;

import com.hdtpt.pentachat.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Lịch sử kết quả của một ván Poker.
 */
@Entity
@Table(name = "poker_round_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoundHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    @Column(name = "winner_id")
    private Long winnerId;

    private Double pot;

    @Column(length = 2000)
    private String communityCards; // AS,KH,2D...

    @Column(length = 2000)
    private String handDescription; // Thùng (Flush), Sảnh (Straight)...
}
