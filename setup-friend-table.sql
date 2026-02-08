-- SQL Script to create friend_requests table
-- Run this script directly in SQL Server Management Studio

USE [WalletDB]
GO

-- Check if friend_requests table exists and drop it
IF OBJECT_ID('dbo.friend_requests', 'U') IS NOT NULL
    DROP TABLE dbo.friend_requests;
GO

-- Create friend_requests table
CREATE TABLE dbo.friend_requests (
    id NVARCHAR(36) PRIMARY KEY NOT NULL,
    from_user_id NVARCHAR(36) NOT NULL,
    to_user_id NVARCHAR(36) NOT NULL,
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2 NOT NULL,
    CONSTRAINT fk_friend_requests_from_user 
        FOREIGN KEY (from_user_id) REFERENCES dbo.users(id),
    CONSTRAINT fk_friend_requests_to_user 
        FOREIGN KEY (to_user_id) REFERENCES dbo.users(id),
    CONSTRAINT unique_friend_request 
        UNIQUE (from_user_id, to_user_id)
);
GO

-- Create indexes for performance
CREATE INDEX idx_friend_requests_to_user_status 
    ON dbo.friend_requests(to_user_id, status);
GO

CREATE INDEX idx_friend_requests_from_user_status 
    ON dbo.friend_requests(from_user_id, status);
GO

PRINT 'friend_requests table created successfully!';
GO

-- Verify table structure
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'friend_requests';
GO

-- Check columns
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'friend_requests'
ORDER BY ORDINAL_POSITION;
GO
