package com.hdtpt.pentachat.games.repository;

import com.hdtpt.pentachat.games.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // JpaRepository đã có sẵn các hàm cơ bản như findAll, save...
    // Nên file này ngắn gọn thế này là đủ dùng rồi.
}