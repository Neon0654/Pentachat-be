# Friend Management API - Test Guide

## 📋 Overview

This guide shows how to test the Friend Management API endpoints. All endpoints are available at `http://localhost:8080/api/friends`.

---

## 🗂️ API Endpoints

### 1. Send Friend Request
**Endpoint:** `POST /api/friends/request`

**Description:** Gửi yêu cầu kết bạn từ user này sang user khác

**Request Body:**
```json
{
  "fromUserId": "user-id-1",
  "toUserId": "user-id-2"
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Friend request sent successfully",
  "data": {
    "id": "request-id-1",
    "fromUserId": "user-id-1",
    "toUserId": "user-id-2",
    "status": "PENDING",
    "createdAt": "2026-02-06T10:30:00",
    "updatedAt": "2026-02-06T10:30:00"
  }
}
```

**Error Cases:**
- 400: User IDs are empty or null
- 400: fromUserId and toUserId are the same
- 400: User does not exist
- 400: Users are already friends
- 400: Friend request already sent

---

### 2. Accept Friend Request
**Endpoint:** `POST /api/friends/accept/{requestId}`

**Description:** Chấp nhận yêu cầu kết bạn → trở thành bạn bè

**URL Parameters:**
- `requestId`: ID của yêu cầu kết bạn cần chấp nhận

**Success Response:**
```json
{
  "success": true,
  "message": "Friend request accepted successfully",
  "data": {
    "id": "request-id-1",
    "fromUserId": "user-id-1",
    "toUserId": "user-id-2",
    "status": "ACCEPTED",
    "createdAt": "2026-02-06T10:30:00",
    "updatedAt": "2026-02-06T10:35:00"
  }
}
```

**Error Cases:**
- 400: Request ID is empty
- 400: Friend request not found
- 400: Friend request is not in PENDING status (already accepted/rejected)

---

### 3. Reject Friend Request
**Endpoint:** `POST /api/friends/reject/{requestId}`

**Description:** Từ chối yêu cầu kết bạn

**URL Parameters:**
- `requestId`: ID của yêu cầu kết bạn cần từ chối

**Success Response:**
```json
{
  "success": true,
  "message": "Friend request rejected successfully",
  "data": {
    "id": "request-id-1",
    "fromUserId": "user-id-1",
    "toUserId": "user-id-2",
    "status": "REJECTED",
    "createdAt": "2026-02-06T10:30:00",
    "updatedAt": "2026-02-06T10:35:00"
  }
}
```

---

### 4. Get Pending Friend Requests
**Endpoint:** `GET /api/friends/pending/{userId}`

**Description:** Lấy danh sách tất cả yêu cầu kết bạn đang chờ xử lý của user

**URL Parameters:**
- `userId`: ID của user

**Success Response:**
```json
{
  "success": true,
  "message": "Pending requests retrieved successfully",
  "data": [
    {
      "id": "request-id-1",
      "fromUserId": "user-id-1",
      "toUserId": "user-id-2",
      "status": "PENDING",
      "createdAt": "2026-02-06T10:30:00",
      "updatedAt": "2026-02-06T10:30:00"
    },
    {
      "id": "request-id-3",
      "fromUserId": "user-id-3",
      "toUserId": "user-id-2",
      "status": "PENDING",
      "createdAt": "2026-02-06T09:15:00",
      "updatedAt": "2026-02-06T09:15:00"
    }
  ]
}
```

---

### 5. Check Friendship Status
**Endpoint:** `GET /api/friends/check/{userId1}/{userId2}`

**Description:** Kiểm tra xem hai user có phải bạn bè không

**URL Parameters:**
- `userId1`: User ID 1
- `userId2`: User ID 2

**Success Response (Friends):**
```json
{
  "success": true,
  "message": "Friendship status retrieved successfully",
  "data": true
}
```

**Success Response (Not Friends):**
```json
{
  "success": true,
  "message": "Friendship status retrieved successfully",
  "data": false
}
```

---

## 🧪 Test Cases

### Test Case 1: Complete Friend Request Workflow
```
1. Send request from user1 to user2
   POST /api/friends/request
   ✓ Status: PENDING

2. Get pending requests for user2
   GET /api/friends/pending/user2-id
   ✓ Should show request from user1

3. Check if they are friends (before accepting)
   GET /api/friends/check/user1-id/user2-id
   ✓ Should return false

4. Accept the request
   POST /api/friends/accept/request-id
   ✓ Status: ACCEPTED

5. Check if they are friends (after accepting)
   GET /api/friends/check/user1-id/user2-id
   ✓ Should return true

6. Get pending requests for user2 (after accepting)
   GET /api/friends/pending/user2-id
   ✓ Should be empty or not contain this request
```

### Test Case 2: Reject Friend Request
```
1. Send request from user1 to user2
   POST /api/friends/request
   ✓ Status: PENDING

2. Reject the request
   POST /api/friends/reject/request-id
   ✓ Status: REJECTED

3. Try to accept rejected request
   POST /api/friends/accept/request-id
   ✗ Should fail with "Friend request is not in PENDING status"
```

### Test Case 3: Error Cases
```
1. Send request to non-existent user
   POST /api/friends/request
   Body: {"fromUserId": "user1", "toUserId": "non-existent"}
   ✗ Should fail: "To user does not exist"

2. Send request to yourself
   POST /api/friends/request
   Body: {"fromUserId": "user1", "toUserId": "user1"}
   ✗ Should fail: "Cannot send friend request to yourself"

3. Send duplicate request
   POST /api/friends/request (twice with same users)
   ✗ Second request should fail: "Friend request already sent"

4. Accept non-existent request
   POST /api/friends/accept/invalid-id
   ✗ Should fail: "Friend request not found"
```

---

## 🔧 Testing with cURL

### Send Friend Request
```bash
curl -X POST http://localhost:8080/api/friends/request \
  -H "Content-Type: application/json" \
  -d '{
    "fromUserId": "user-1",
    "toUserId": "user-2"
  }'
```

### Accept Friend Request
```bash
curl -X POST http://localhost:8080/api/friends/accept/{requestId}
```

### Reject Friend Request
```bash
curl -X POST http://localhost:8080/api/friends/reject/{requestId}
```

### Get Pending Requests
```bash
curl -X GET http://localhost:8080/api/friends/pending/{userId}
```

### Check Friendship
```bash
curl -X GET http://localhost:8080/api/friends/check/{userId1}/{userId2}
```

---

## 🧬 Running Unit Tests

Run all friend service tests:
```bash
cd Testing-Lab
./mvnw test -Dtest=FriendServiceTest
```

Run all friend controller tests:
```bash
cd Testing-Lab
./mvnw test -Dtest=FriendControllerTest
```

Run all tests:
```bash
cd Testing-Lab
./mvnw test
```

---

## 📊 SQL Queries for Verification

### View all friend requests:
```sql
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
```

### Get pending requests for a user:
```sql
SELECT 
    fr.id,
    u1.username AS 'From User',
    fr.status,
    fr.created_at
FROM friend_requests fr
JOIN users u1 ON fr.from_user_id = u1.id
WHERE fr.to_user_id = 'user-id'
  AND fr.status = 'PENDING';
```

### Get accepted friendships:
```sql
SELECT 
    u1.username AS 'User 1',
    u2.username AS 'User 2',
    fr.status,
    fr.created_at AS 'Friend Since'
FROM friend_requests fr
JOIN users u1 ON fr.from_user_id = u1.id
JOIN users u2 ON fr.to_user_id = u2.id
WHERE fr.status = 'ACCEPTED'
ORDER BY fr.created_at DESC;
```

---

## ✅ Success Criteria

- ✓ Gửi lời mời kết bạn thành công
- ✓ Chấp nhận yêu cầu → trở thành bạn bè
- ✓ Từ chối yêu cầu
- ✓ Lấy danh sách yêu cầu đang chờ
- ✓ Kiểm tra quan hệ bạn bè
- ✓ Validation đầu vào
- ✓ Error handling và messages rõ ràng
