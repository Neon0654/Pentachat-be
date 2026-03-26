package com.hdtpt.pentachat.games.poker.repository;

import com.hdtpt.pentachat.games.poker.model.RoundHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository lưu trữ lịch sử các ván Poker.
 */
@Repository
public interface RoundHistoryRepository extends JpaRepository<RoundHistory, Long> {
    List<RoundHistory> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}
