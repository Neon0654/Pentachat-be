# Spring Boot RESTful API - Wallet System

A production-ready Spring Boot RESTful API with clean layered architecture, featuring user authentication and wallet management system.

## 🏗️ Architecture Overview

### Layered Architecture
```
Controller Layer
     ↓
Service Layer
     ↓
Data Access Layer (DataApi Interface)
     ↓
MockDataApiImpl / JpaDataApiImpl (Implementations)
     ↓
MockDataStore / Database
```

### Key Design Principles

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Abstraction**: Services depend on DataApi interface, not concrete implementations
3. **Easy Testing**: Mock implementation can be swapped without changing business logic
4. **Easy Database Migration**: Switch from mock data to real database by only implementing DataApi

### Folder Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── controller/              # REST endpoints
│   │   ├── AuthController.java
│   │   └── WalletController.java
│   ├── service/                 # Business logic
│   │   ├── AuthService.java
│   │   └── WalletService.java
│   ├── dataaccess/              # Data abstraction
│   │   ├── DataApi.java         # Interface
│   │   └── MockDataApiImpl.java  # Mock implementation
│   ├── datastore/               # In-memory storage
│   │   └── MockDataStore.java
│   ├── model/                   # Entity models
│   │   ├── User.java
│   │   ├── Wallet.java
│   │   └── Transaction.java
│   ├── dto/                     # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── DepositRequest.java
│   │   │   ├── WithdrawRequest.java
│   │   │   └── TransferRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── BalanceResponse.java
│   │       ├── TransactionResponse.java
│   │       └── ApiResponse.java
│   ├── exception/               # Exception handling
│   │   ├── AppException.java
│   │   └── GlobalExceptionHandler.java
│   ├── util/                    # Utilities
│   │   └── IdGenerator.java
│   └── ProjectGaugeApplication.java
├── pom.xml
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Spring Boot 4.0.2

### Installation

1. **Clone or navigate to project**
```bash
cd demo
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Mock Data

The application initializes with mock users:
- **User 1**: username: `alice`, password: `password123`, balance: `$1000`
- **User 2**: username: `bob`, password: `password456`, balance: `$500`

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Response Format

All responses follow this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

---

## 🔐 Authentication Endpoints

### 1. Register User

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "username": "newuser",
  "password": "password123"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "newuser",
    "message": "Registration successful"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Username already exists"
}
```

**Validation Rules:**
- `username`: Required, cannot be empty
- `password`: Required, cannot be empty
- `username`: Must be unique

---

### 2. Login User

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "alice",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "alice",
    "message": "Login successful"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Invalid username or password"
}
```

---

## 💰 Wallet Endpoints

### 3. Get Wallet Balance

**Endpoint:** `GET /wallet/balance?userId={userId}`

**Query Parameters:**
- `userId` (required): User's ID (obtained from login/register)

**Example Request:**
```bash
GET /wallet/balance?userId=550e8400-e29b-41d4-a716-446655440001
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Balance retrieved successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "balance": 1000.00
  }
}
```

---

### 4. Deposit Money

**Endpoint:** `POST /wallet/deposit?userId={userId}`

**Query Parameters:**
- `userId` (required): User's ID

**Request Body:**
```json
{
  "amount": 500.00
}
```

**Example Request:**
```bash
POST /wallet/deposit?userId=550e8400-e29b-41d4-a716-446655440001
Content-Type: application/json

{
  "amount": 500.00
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Deposit successful. New balance: 1500.0",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "balance": 1500.00
  }
}
```

**Validation Rules:**
- `amount`: Required, must be greater than 0

---

### 5. Withdraw Money

**Endpoint:** `POST /wallet/withdraw?userId={userId}`

**Query Parameters:**
- `userId` (required): User's ID

**Request Body:**
```json
{
  "amount": 200.00
}
```

**Example Request:**
```bash
POST /wallet/withdraw?userId=550e8400-e29b-41d4-a716-446655440001
Content-Type: application/json

{
  "amount": 200.00
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Withdrawal successful. New balance: 1300.0",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "balance": 1300.00
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Insufficient balance. Current balance: 1500.0"
}
```

**Validation Rules:**
- `amount`: Required, must be greater than 0
- User must have sufficient balance

---

### 6. Transfer Money

**Endpoint:** `POST /wallet/transfer?fromUserId={userId}`

**Query Parameters:**
- `fromUserId` (required): Sender's User ID

**Request Body:**
```json
{
  "toUsername": "bob",
  "amount": 250.00
}
```

**Example Request:**
```bash
POST /wallet/transfer?fromUserId=550e8400-e29b-41d4-a716-446655440001
Content-Type: application/json

{
  "toUsername": "bob",
  "amount": 250.00
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Transfer successful. New balance: 1050.0",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "balance": 1050.00
  }
}
```

**Error Responses:**
```json
{
  "success": false,
  "message": "Recipient not found"
}
```

```json
{
  "success": false,
  "message": "Cannot transfer to yourself"
}
```

```json
{
  "success": false,
  "message": "Insufficient balance. Current balance: 1500.0"
}
```

**Validation Rules:**
- `toUsername`: Required, recipient must exist
- `amount`: Required, must be greater than 0
- Sender must have sufficient balance
- Sender and recipient cannot be the same

---

### 7. Get Transaction History

**Endpoint:** `GET /wallet/transactions?userId={userId}`

**Query Parameters:**
- `userId` (required): User's ID

**Example Request:**
```bash
GET /wallet/transactions?userId=550e8400-e29b-41d4-a716-446655440001
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "type": "DEPOSIT",
      "fromUserId": "550e8400-e29b-41d4-a716-446655440001",
      "toUserId": null,
      "amount": 500.00,
      "createdAt": "2025-01-24T10:30:00"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440011",
      "type": "WITHDRAW",
      "fromUserId": "550e8400-e29b-41d4-a716-446655440001",
      "toUserId": null,
      "amount": 200.00,
      "createdAt": "2025-01-24T10:35:00"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440012",
      "type": "TRANSFER",
      "fromUserId": "550e8400-e29b-41d4-a716-446655440001",
      "toUserId": "550e8400-e29b-41d4-a716-446655440002",
      "amount": 250.00,
      "createdAt": "2025-01-24T10:40:00"
    }
  ]
}
```

---

## 📝 HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful GET, POST, PUT requests |
| 201 | Created | Successful resource creation (POST) |
| 400 | Bad Request | Invalid input, validation errors |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

---

## 🔄 Switching from Mock Data to Real Database

### Current Architecture (Mock Data)

```
DataApi (Interface)
└── MockDataApiImpl
    └── MockDataStore
```

### To switch to real database (e.g., PostgreSQL + JPA):

1. **Create new implementation:**
```java
@Component
public class JpaDataApiImpl implements DataApi {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Override
    public User createUser(String username, String password) {
        // JPA implementation
    }
    // ... implement all methods
}
```

2. **Create JPA Repositories:**
```java
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
```

3. **No changes needed** in:
- `AuthController`
- `WalletController`
- `AuthService`
- `WalletService`

Service and Controller layers will automatically use the new implementation!

---

## 🧪 Testing the API

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "charlie", "password": "pass789"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "password123"}'
```

**Get Balance:**
```bash
curl http://localhost:8080/wallet/balance?userId=550e8400-e29b-41d4-a716-446655440001
```

**Deposit:**
```bash
curl -X POST http://localhost:8080/wallet/deposit?userId=550e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
```

**Withdraw:**
```bash
curl -X POST http://localhost:8080/wallet/withdraw?userId=550e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json" \
  -d '{"amount": 50.00}'
```

**Transfer:**
```bash
curl -X POST "http://localhost:8080/wallet/transfer?fromUserId=550e8400-e29b-41d4-a716-446655440001" \
  -H "Content-Type: application/json" \
  -d '{"toUsername": "bob", "amount": 100.00}'
```

**Get Transactions:**
```bash
curl http://localhost:8080/wallet/transactions?userId=550e8400-e29b-41d4-a716-446655440001
```

### Using Postman

1. Import the API endpoints into Postman
2. Test each endpoint with provided request/response examples
3. Verify error handling and validation

---

## 🎯 Key Features

✅ **Layered Architecture**
- Clear separation: Controller → Service → DataAccess
- Easy to test and maintain

✅ **Abstraction with DataApi Interface**
- Business logic independent of data source
- Easy to switch implementations

✅ **Mock Data Support**
- In-memory database with MockDataStore
- Perfect for development and testing

✅ **Input Validation**
- Jakarta validation annotations
- Comprehensive error messages

✅ **Error Handling**
- Global exception handler
- Consistent error responses

✅ **RESTful API**
- Proper HTTP methods and status codes
- Clean JSON responses

✅ **Simple Authorization**
- Users can only access their own wallet
- Validated through userId parameter

✅ **Transaction Tracking**
- All wallet operations recorded
- Complete transaction history

---

## 📋 Design Decisions

1. **No Spring Security** - Simple authorization for demonstration
2. **Plain text passwords** - Mock data only; use bcrypt in production
3. **Query parameters for userId** - In production, use JWT tokens
4. **In-memory storage** - Easy to replace with database
5. **Lombok annotations** - Reduces boilerplate code
6. **No database** - Keeps project simple for learning

---

## 🔮 Future Enhancements

- [ ] Add Spring Security with JWT authentication
- [ ] Implement password hashing (bcrypt)
- [ ] Add database support (PostgreSQL + JPA)
- [ ] Add unit tests
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Add transaction sorting and filtering
- [ ] Add transaction export (CSV/PDF)
- [ ] Add notification system
- [ ] Add transaction approval workflow

---

## 📞 Support

For issues or questions, please create an issue in the repository.

---

**Built with ❤️ using Spring Boot 4.0.2 + Java 17**
