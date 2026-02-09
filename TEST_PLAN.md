# Pentachat Project - Test Plan & Flow

## 1. Overview
This document describes the test strategy, scenarios, and flows for the Pentachat application. The tests are organized by domain to ensure modularity and clarity.

## 2. Test Scenarios by Domain

### A. Identity Domain
**Goal**: Ensure secure authentication and user management.
- **Login Flow**:
    1. Register user.
    2. Attempt login with correct credentials -> Success.
    3. Attempt login with wrong password -> Failure.
- **User Search Flow**:
    1. Create multiple users.
    2. Search for a part of the username -> Verify results.

### B. Finance Domain (Wallet)
**Goal**: Validate financial integrity and transaction history.
- **Transaction Flow**:
    1. Check initial balance (0).
    2. Add funds (Deposit) -> Verify balance increase.
    3. Transfer funds from User A to User B -> Verify A decrease, B increase.
    4. Attempt transfer with insufficient funds -> Failure.

### C. Messaging Domain
**Goal**: Reliable real-time communication.
- **P2P Message Flow**:
    1. User A sends message to User B.
    2. Verify message exists in User B's inbox.
    3. Verify message is marked as "Unread" initially.

### D. Groups Domain
**Goal**: Collaborative chat management.
- **Group Creation Flow**:
    1. User A creates Group "Game Night".
    2. User A adds User B and User C to the group.
    3. Verify all 3 members see the group.
    4. Verify non-members cannot see group messages.

### E. Games Domain
**Goal**: Interactive features logic.
- **Rock Paper Scissors Flow**:
    1. User A invites User B to a game.
    2. Both users make moves.
    3. System determines winner based on rules (Rock > Scissors, etc.).
    4. Points awarded to winner.

## 3. Test Structure
Tests are located in `src/test/java/com/hdtpt/pentachat/` organized as follows:
- `identity/service/*.java`
- `finance/service/*.java`
- `message/service/*.java`
- `groups/service/*.java`
- `games/service/*.java`

## 4. How to Run
Use Maven command:
```powershell
mvn test
```
To run a specific test:
```powershell
mvn test -Dtest=UserServiceTest
```
