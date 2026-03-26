package com.hdtpt.pentachat.games.poker.model;

/**
 * Các giai đoạn của một ván bài Poker.
 */
public enum GamePhase {
    WAITING,    // Đang chờ người chơi
    DEALING,    // Đang chia bài tẩy (Hole cards)
    PRE_FLOP,   // Vòng cược đầu tiên
    FLOP,       // Chia 3 lá bài chung đầu tiên
    TURN,       // Chia lá bài chung thứ 4
    RIVER,      // Chia lá bài chung thứ 5
    SHOWDOWN,   // Ngửa bài xác định người thắng
    ENDED       // Kết thúc ván
}
