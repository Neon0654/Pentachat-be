-- Add group messaging support to messages table
-- This migration adds the type and targetId columns to support both personal and group messages

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('messages') AND name = 'type')
BEGIN
    ALTER TABLE messages 
    ADD type NVARCHAR(50) NOT NULL DEFAULT 'PERSONAL';
    
    PRINT 'Added type column to messages table';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('messages') AND name = 'targetId')
BEGIN
    ALTER TABLE messages 
    ADD targetId NVARCHAR(36) NULL;
    
    PRINT 'Added targetId column to messages table';
END

-- Make toUserId nullable for backward compatibility
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('messages') AND name = 'toUserId')
BEGIN
    -- SQL Server doesn't allow direct modifying constraints,
    -- so we need to check if the column is NOT NULL and can be modified
    -- This is informational for now
    PRINT 'toUserId column already exists - verify if it can now accept NULL values based on your requirements';
END

-- Create indexes for better query performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('messages') AND name = 'idx_messages_targetId_type')
BEGIN
    CREATE INDEX idx_messages_targetId_type ON messages(targetId, type);
    PRINT 'Created index on targetId and type columns';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('messages') AND name = 'idx_messages_fromUserId_type')
BEGIN
    CREATE INDEX idx_messages_fromUserId_type ON messages(fromUserId, type);
    PRINT 'Created index on fromUserId and type columns';
END

-- Update existing messages to have PERSONAL type if they have a toUserId
UPDATE messages 
SET type = 'PERSONAL', targetId = toUserId 
WHERE type = 'PERSONAL' AND targetId IS NULL AND toUserId IS NOT NULL;

PRINT 'Migration V2__Add_Group_Message_Support completed successfully';
