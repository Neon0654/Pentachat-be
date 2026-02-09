-- Test Data Script
-- Run this after setup-friend-table.sql to insert test data

USE [WalletDB]
GO

-- Insert test data into users table (if not already exists)
-- First check and insert test users
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser1')
BEGIN
    INSERT INTO users (id, username, password, createdAt, updatedAt)
    VALUES (
        NEWID(), 
        'testuser1', 
        'password123',  -- In real app, this should be hashed
        GETDATE(),
        GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser2')
BEGIN
    INSERT INTO users (id, username, password, createdAt, updatedAt)
    VALUES (
        NEWID(), 
        'testuser2', 
        'password123',
        GETDATE(),
        GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser3')
BEGIN
    INSERT INTO users (id, username, password, createdAt, updatedAt)
    VALUES (
        NEWID(), 
        'testuser3', 
        'password123',
        GETDATE(),
        GETDATE()
    );
END
GO

-- Get the user IDs for testing
DECLARE @user1Id NVARCHAR(36);
DECLARE @user2Id NVARCHAR(36);
DECLARE @user3Id NVARCHAR(36);

SELECT @user1Id = id FROM users WHERE username = 'testuser1';
SELECT @user2Id = id FROM users WHERE username = 'testuser2';
SELECT @user3Id = id FROM users WHERE username = 'testuser3';

-- Insert test friend requests
-- Test Case 1: Pending request from user1 to user2
INSERT INTO friend_requests (id, from_user_id, to_user_id, status, created_at, updated_at)
VALUES (
    NEWID(),
    @user1Id,
    @user2Id,
    'PENDING',
    GETDATE(),
    GETDATE()
);

-- Test Case 2: Accepted request from user1 to user3
INSERT INTO friend_requests (id, from_user_id, to_user_id, status, created_at, updated_at)
VALUES (
    NEWID(),
    @user1Id,
    @user3Id,
    'ACCEPTED',
    DATEADD(DAY, -1, GETDATE()),
    GETDATE()
);

-- Test Case 3: Rejected request from user2 to user3
INSERT INTO friend_requests (id, from_user_id, to_user_id, status, created_at, updated_at)
VALUES (
    NEWID(),
    @user2Id,
    @user3Id,
    'REJECTED',
    DATEADD(DAY, -2, GETDATE()),
    GETDATE()
);

PRINT 'Test data inserted successfully!';
GO

-- Query to verify test data
SELECT 
    fr.id,
    u1.username AS 'From User',
    u2.username AS 'To User',
    fr.status,
    fr.created_at,
    fr.updated_at
FROM friend_requests fr
JOIN users u1 ON fr.from_user_id = u1.id
JOIN users u2 ON fr.to_user_id = u2.id
ORDER BY fr.created_at DESC;
GO
