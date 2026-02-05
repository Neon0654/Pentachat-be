# 📋 Profile API Test Documentation

## API Endpoints

### 1. GET /api/profiles/{userId}
**Mục đích:** Lấy thông tin cá nhân của user

**Request:**
```
GET /api/profiles/user-id-123
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "profile-id-456",
    "userId": "user-id-123",
    "fullName": "John Doe",
    "bio": "Software Engineer",
    "avatar": "https://example.com/avatar.jpg",
    "phoneNumber": "0912345678",
    "address": "123 Main St, City",
    "createdAt": "2026-02-05T14:00:00",
    "updatedAt": "2026-02-05T14:00:00"
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Profile not found",
  "data": null
}
```

---

### 2. PUT /api/profiles/{userId}
**Mục đích:** Cập nhật thông tin cá nhân

**Request:**
```
PUT /api/profiles/user-id-123
Content-Type: application/json

{
  "fullName": "Jane Doe",
  "bio": "Senior Developer",
  "avatar": "https://example.com/new-avatar.jpg",
  "phoneNumber": "0987654321",
  "address": "456 Oak Ave, City"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": "profile-id-456",
    "userId": "user-id-123",
    "fullName": "Jane Doe",
    "bio": "Senior Developer",
    "avatar": "https://example.com/new-avatar.jpg",
    "phoneNumber": "0987654321",
    "address": "456 Oak Ave, City",
    "createdAt": "2026-02-05T14:00:00",
    "updatedAt": "2026-02-05T15:30:00"
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Profile not found",
  "data": null
}
```

---

## Code Review Checklist

### ✅ Profile Entity (Database)
- [x] Table name: `profiles` (snake_case)
- [x] Primary key: `id` (String UUID)
- [x] Foreign key: `userId` (String, unique)
- [x] Fields: fullName, bio, avatar, phoneNumber, address
- [x] Timestamps: createdAt, updatedAt
- [x] Lombok annotations: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

### ✅ ProfileRepository
- [x] Extends JpaRepository<Profile, String>
- [x] Method: findByUserId(String userId) - Optional<Profile>

### ✅ ProfileService
- [x] Method: getProfile(userId) - returns ProfileResponse
  - Throws AppException if profile not found
- [x] Method: updateProfile(userId, data) - returns ProfileResponse
  - Supports partial updates (null fields ignored)
  - Updates updatedAt timestamp
  - Throws AppException if profile not found
- [x] Method: createProfile(userId) - auto-called on user registration
  - Creates profile with UUID
  - Sets createdAt and updatedAt
- [x] Constructor Injection (best practice)

### ✅ ProfileController
- [x] Base path: `/api/profiles`
- [x] GET /{userId} - calls getProfile()
- [x] PUT /{userId} - calls updateProfile()
- [x] Returns ApiResponse format (success, message, data)
- [x] Constructor Injection

### ✅ DTOs
- [x] UpdateProfileRequest - all fields optional (supports partial updates)
- [x] ProfileResponse - complete profile information

### ✅ Integration
- [x] AuthService auto-creates profile on user registration
- [x] ProfileService injected in AuthService
- [x] No hard-coded data

### ✅ Quy ước Tuân thủ
- [x] Package naming: lowercase (com.hdtpt.pentachat.profile)
- [x] Class naming: PascalCase
- [x] Method naming: camelCase
- [x] Database table: snake_case plural (profiles)
- [x] No @Autowired (Constructor Injection only)
- [x] Exception handling with AppException
- [x] ApiResponse format standard

---

## Test Steps (Manual via Postman/cURL)

### Step 1: Register User
```
POST /api/auth/register
{
  "username": "testuser",
  "password": "password123"
}
```
Response includes `userId`. Copy this for next steps.

### Step 2: Get Profile (Should exist after registration)
```
GET /api/profiles/{userId}
```
Verify profile is created automatically with empty fields.

### Step 3: Update Profile
```
PUT /api/profiles/{userId}
{
  "fullName": "Test User",
  "bio": "Hello World",
  "phoneNumber": "0123456789"
}
```
Verify response shows updated data with new `updatedAt` timestamp.

### Step 4: Get Profile Again
```
GET /api/profiles/{userId}
```
Verify updated data is persisted.

---

## Database Verification

After running the application, check SQL Server:

```sql
SELECT * FROM profiles;

-- Expected columns:
-- id (PK, nvarchar)
-- userId (UK, nvarchar)
-- fullName (nvarchar, nullable)
-- bio (nvarchar, nullable)
-- avatar (nvarchar, nullable)
-- phoneNumber (nvarchar, nullable)
-- address (nvarchar, nullable)
-- createdAt (datetime2)
-- updatedAt (datetime2)
```

---

## Summary

✅ **All Task Requirements Met:**
- ✅ Database table `profiles` created
- ✅ getProfile(userId) function implemented
- ✅ updateProfile(userId, data) function implemented
- ✅ API GET /api/profiles/{userId} ready
- ✅ API PUT /api/profiles/{userId} ready
- ✅ Auto-create profile on registration
- ✅ Follows all code conventions
- ✅ Proper error handling
- ✅ ApiResponse format compliant

**Ready for deployment!**
