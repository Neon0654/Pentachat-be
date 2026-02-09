/**
 * Mock Data for Pentachat Frontend Testing
 * Reference this file to understand the expected data formats
 */

const MOCK_DATA = {
    users: [
        { id: 1, username: "alice", email: "alice@example.com", createdAt: "2026-02-09T10:00:00" },
        { id: 2, username: "bob", email: "bob@example.com", createdAt: "2026-02-09T10:05:00" },
        { id: 3, username: "charlie", email: "charlie@example.com", createdAt: "2026-02-09T10:10:00" }
    ],
    wallets: [
        { userId: 1, balance: 1000.0, currency: "PENTA" },
        { userId: 2, balance: 500.0, currency: "PENTA" },
        { userId: 3, balance: 0.0, currency: "PENTA" }
    ],
    messages: [
        { id: 101, fromUserId: 1, toUserId: 2, content: "Hello Bob!", type: "PERSONAL", isRead: true },
        { id: 102, fromUserId: 2, toUserId: 1, content: "Hi Alice, what's up?", type: "PERSONAL", isRead: false }
    ],
    groups: [
        { id: 1, name: "Developers", creatorId: 1, memberIds: [1, 2, 3] }
    ],
    gameResults: [
        { roomId: "R123", player1: "alice", player2: "bob", winner: "alice", move1: "ROCK", move2: "SCISSORS" }
    ]
};

// Exporting for use in JS environments
if (typeof module !== 'undefined') {
    module.exports = MOCK_DATA;
}
