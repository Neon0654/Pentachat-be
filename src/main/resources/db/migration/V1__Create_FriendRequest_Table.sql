-- Create friend_requests table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'friend_requests')
BEGIN
    CREATE TABLE friend_requests (
        id NVARCHAR(36) PRIMARY KEY NOT NULL,
        from_user_id NVARCHAR(36) NOT NULL,
        to_user_id NVARCHAR(36) NOT NULL,
        status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
        created_at DATETIME2 NOT NULL,
        updated_at DATETIME2 NOT NULL,
        CONSTRAINT fk_from_user FOREIGN KEY (from_user_id) REFERENCES users(id),
        CONSTRAINT fk_to_user FOREIGN KEY (to_user_id) REFERENCES users(id),
        CONSTRAINT unique_friend_request UNIQUE (from_user_id, to_user_id)
    );

    -- Create indexes for performance
    CREATE INDEX idx_friend_requests_to_user ON friend_requests(to_user_id, status);
    CREATE INDEX idx_friend_requests_from_user ON friend_requests(from_user_id, status);
    
    PRINT 'friend_requests table created successfully';
END
ELSE
BEGIN
    PRINT 'friend_requests table already exists';
END
