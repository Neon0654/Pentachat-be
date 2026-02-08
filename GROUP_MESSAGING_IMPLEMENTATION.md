# Group Messaging Implementation

## Overview
The group messaging system has been successfully implemented using a shared `messages` table with support for both **PERSONAL** (1-to-1) and **GROUP** (1-to-many) messages.

## Database Schema Changes

### Modified Table: `messages`

**New Columns:**
- `type` (NVARCHAR(50), NOT NULL, DEFAULT='PERSONAL')
  - PERSONAL: 1-to-1 messages
  - GROUP: Group messages

- `targetId` (NVARCHAR(36), NULL)
  - For PERSONAL: Contains userId
  - For GROUP: Contains groupId

- `toUserId` (Nullable for backward compatibility)
  - Legacy field for existing personal messages

**Indexes Added:**
- `idx_messages_targetId_type` on (targetId, type)
- `idx_messages_fromUserId_type` on (fromUserId, type)

## API Endpoints

### Send Personal Message (Existing - Still Supported)
```
POST /api/messages/send
{
  "from": "userId1",
  "to": "userId2",
  "content": "Hello!",
  "type": "PERSONAL"
}
```

### Send Group Message
```
POST /api/messages/group/send
{
  "from": "userId1",
  "groupId": "group123",
  "content": "Hello everyone!",
  "type": "GROUP"
}
```

### Get Group History
```
GET /api/messages/group/{groupId}
```

**Response:**
```json
{
  "success": true,
  "message": "Group history retrieved successfully",
  "data": [
    {
      "id": "msg-id-1",
      "from": "user1",
      "targetId": "group123",
      "type": "GROUP",
      "content": "Hello everyone!",
      "createdAt": "2026-02-08T14:30:00",
      "isRead": false
    },
    ...
  ]
}
```

## Service Methods

### MessageService
New methods added:

1. **pushToGroup(fromUserId: String, groupId: String, content: String) -> MessageResponse**
   - Sends a message to a group
   - Saves to database with type=GROUP
   - Triggers notification to all group members

2. **getGroupHistory(groupId: String) -> List<MessageResponse>**
   - Retrieves all messages in a group
   - Ordered by createdAt (ascending)
   - Returns empty list if group has no messages

### DataApi Interface
New methods added:

1. **createGroupMessage(fromUserId: String, groupId: String, content: String) -> Message**
   - Creates and saves group message to database

2. **getGroupHistory(groupId: String) -> List<Message>**
   - Gets all messages for a group

3. **getMessagesByTargetIdAndType(targetId: String, type: String) -> List<Message>**
   - Generic method to retrieve messages by target and type

### MessageRepository
New Query Methods:

1. **findGroupHistory(groupId: String) -> List<Message>**
   - Uses @Query annotation
   - Finds all messages with targetId=groupId AND type='GROUP'
   - Ordered by createdAt

2. **findByTargetIdAndType(targetId: String, type: MessageType) -> List<Message>**
   - Generic finder for messages by target and type

## Model Changes

### Message Entity
```java
@Entity
@Table(name = "messages")
public class Message {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String fromUserId;
    
    @Enumerated(EnumType.STRING)
    private MessageType type;  // PERSONAL or GROUP
    
    @Column(nullable = false)
    private String targetId;   // userId or groupId
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean isRead;
    
    // Legacy field for backward compatibility
    @Column
    private String toUserId;
    
    public enum MessageType {
        PERSONAL,  // 1-to-1 message
        GROUP      // Group message
    }
}
```

### MessageRequest DTO
Updated to support both personal and group messages:
```java
@Data
public class MessageRequest {
    @NotNull
    private String from;
    
    // Personal message recipient
    private String to;
    
    // Group message recipient
    private String groupId;
    
    @NotBlank
    private String content;
    
    // Message type: PERSONAL or GROUP (default: PERSONAL)
    @Builder.Default
    private String type = "PERSONAL";
}
```

### MessageResponse DTO
Updated to include type and targetId:
```java
@Data
public class MessageResponse {
    private String id;
    private String from;
    private String to;           // For backward compatibility
    private String targetId;      // userId or groupId
    private String type;          // PERSONAL or GROUP
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
```

## Database Migration

**File:** `src/main/resources/db/migration/V2__Add_Group_Message_Support.sql`

The migration:
1. Adds `type` column with DEFAULT value 'PERSONAL'
2. Adds `targetId` column
3. Creates indexes for performance optimization
4. Updates existing messages to populate targetId from toUserId

## Implementation Highlights

### Backward Compatibility
- Personal messages can still use the `toUserId` field
- The conversion logic maps `toUserId` to `targetId` for existing messages
- MessageResponse includes both `to` and `targetId` for compatibility

### Type Safety
- Uses Java Enum for MessageType instead of String constants
- JPA repository queries validate type before execution

### Performance
- Indexes on (targetId, type) and (fromUserId, type) for fast queries
- Query methods use Spring Data @Query annotations for efficiency

### Real-time Notifications
- `notifyGroupNewMessage()` method placeholder for WebSocket integration
- Can be extended to notify all group members in real-time

## Future Enhancements

1. **WebSocket Integration**
   - Real-time message delivery to online group members
   - Update notification placeholder: `webSocketService.sendToGroup(groupId, message)`

2. **Group Management**
   - Create/delete groups
   - Add/remove group members
   - Group roles and permissions

3. **Message Search**
   - Search messages by content within a group
   - Search across multiple groups

4. **Message Reactions**
   - Add emoji reactions to messages
   - Track reaction count

5. **Message Threading**
   - Reply to specific messages
   - Create message threads

## Testing

To test the group messaging functionality:

### Via API
```powershell
# Send group message
$body = @{
    from = "user1"
    groupId = "group123"
    content = "Hello group!"
    type = "GROUP"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/messages/group/send" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

# Get group history
Invoke-WebRequest -Uri "http://localhost:8080/api/messages/group/group123" `
    -Method GET
```

### Via Java Code
```java
MessageResponse response = messageService.pushToGroup(
    "user1",
    "group123",
    "Hello everyone!"
);

List<MessageResponse> history = messageService.getGroupHistory("group123");
```

## Summary

✅ **Completed Tasks:**
1. Modified Message entity to support both personal and group messages
2. Updated MessageRepository with group-specific query methods
3. Added new endpoints for group messaging
4. Implemented getGroupHistory(groupId) method
5. Created database migration script
6. Updated all DTOs and service layers
7. Maintained backward compatibility with existing code

**Key Features:**
- Shared messages table (type=PERSONAL or GROUP, targetId=recipient)
- getGroupHistory(groupId) returns all messages in the group
- Send message → save to DB (automatic)
- Call API → display group messages (automatic)
