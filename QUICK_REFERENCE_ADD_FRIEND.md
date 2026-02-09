# 🎯 Friend System - Quick Reference

## Database
```sql
-- Create table
friend_requests (
  id: UUID,
  from_user_id: VARCHAR,
  to_user_id: VARCHAR,
  status: PENDING|ACCEPTED|REJECTED,
  created_at, updated_at: DATETIME
)
```

## Service Methods
```java
// Gửi lời mời
FriendRequest sendFriendRequest(String fromId, String toId)

// Chấp nhận
FriendRequest acceptFriend(String requestId)

// Từ chối
FriendRequest rejectFriend(String requestId)

// Danh sách chờ
List<FriendRequest> getPendingRequests(String userId)

// Kiểm tra bạn bè
boolean areFriends(String userId1, String userId2)
```

## API Endpoints
| Method | Endpoint | Action |
|--------|----------|--------|
| POST | `/api/friends/request` | Gửi lời mời |
| POST | `/api/friends/accept/{id}` | Chấp nhận |
| POST | `/api/friends/reject/{id}` | Từ chối |
| GET | `/api/friends/pending/{id}` | Danh sách chờ |
| GET | `/api/friends/check/{id1}/{id2}` | Kiểm tra bạn bè |

## Status
- ✅ **31/31 Tests Passed** (1 + 17 Service + 10 Controller)
- ✅ **All Features Working**
- ✅ **Error Handling Complete**
- ✅ **Documentation Ready**

## Start Testing
```bash
cd Testing-Lab
./mvnw spring-boot:run
# Then access: http://localhost:8080/api/friends/...
```

## Run Tests
```bash
./mvnw test
```

## Database Setup
```bash
# Run SQL script:
setup-friend-table.sql
setup-test-data.sql
```

---

**See FRIEND_API_TEST_GUIDE.md for full documentation**
