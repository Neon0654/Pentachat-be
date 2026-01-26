# 🚀 Quick Start Guide

## Project Setup

### 1. Build and Run

```bash
# Navigate to project directory
cd demo

# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

---

## 📊 Complete Workflow Example

### Step 1: Register a New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "john123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid-here",
    "username": "john",
    "message": "Registration successful"
  }
}
```

**Save the `id` value for next steps!**

---

### Step 2: Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "john123"
  }'
```

---

### Step 3: Check Balance

```bash
# Replace {userId} with the id from Step 1
curl http://localhost:8080/wallet/balance?userId={userId}
```

**Response:**
```json
{
  "success": true,
  "message": "Balance retrieved successfully",
  "data": {
    "userId": "{userId}",
    "balance": 0.0
  }
}
```

---

### Step 4: Deposit Money

```bash
curl -X POST http://localhost:8080/wallet/deposit?userId={userId} \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00
  }'
```

---

### Step 5: Withdraw Money

```bash
curl -X POST http://localhost:8080/wallet/withdraw?userId={userId} \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200.00
  }'
```

---

### Step 6: Transfer to Another User

```bash
# Transfer to existing user "alice"
curl -X POST http://localhost:8080/wallet/transfer?fromUserId={userId} \
  -H "Content-Type: application/json" \
  -d '{
    "toUsername": "alice",
    "amount": 100.00
  }'
```

---

### Step 7: View Transaction History

```bash
curl http://localhost:8080/wallet/transactions?userId={userId}
```

**Response:**
```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": "trans-1",
      "type": "DEPOSIT",
      "fromUserId": "{userId}",
      "toUserId": null,
      "amount": 1000.00,
      "createdAt": "2025-01-24T10:30:00"
    },
    {
      "id": "trans-2",
      "type": "WITHDRAW",
      "fromUserId": "{userId}",
      "toUserId": null,
      "amount": 200.00,
      "createdAt": "2025-01-24T10:31:00"
    },
    {
      "id": "trans-3",
      "type": "TRANSFER",
      "fromUserId": "{userId}",
      "toUserId": "alice-id",
      "amount": 100.00,
      "createdAt": "2025-01-24T10:32:00"
    }
  ]
}
```

---

## 👥 Pre-loaded Mock Users

The application comes with two pre-loaded users for testing:

| Username | Password | Initial Balance |
|----------|----------|-----------------|
| alice    | password123 | $1000 |
| bob      | password456 | $500 |

You can login with these credentials immediately!

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "password123"
  }'
```

---

## 🏗️ Architecture at a Glance

```
┌─────────────────────────────────────────────┐
│           REST API Endpoints                │
│ (/auth/register, /auth/login, /wallet/...) │
└────────────────┬────────────────────────────┘
                 │
        ┌────────▼─────────┐
        │  Controllers     │
        │ (HTTP handlers)  │
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │  Services        │
        │ (Business logic) │
        └────────┬─────────┘
                 │
        ┌────────▼──────────────┐
        │   DataApi Interface   │
        │  (Abstraction layer)  │
        └────────┬──────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼──────────────┐   ┌─────▼────────────┐
│ MockDataApiImpl   │   │ JpaDataApiImpl    │
│ (Development)    │   │ (Production)     │
└───┬──────────────┘   └──────────────────┘
    │
┌───▼────────────────┐
│ MockDataStore      │
│ (In-memory DB)     │
└────────────────────┘
```

**Key Point:** Switch from mock data to database by just changing the `@Component` implementation!

---

## 🧪 Testing Scenarios

### Scenario 1: Complete Wallet Operations

```bash
# Register
USER_ID=$(curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "password": "test123"}' | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)

# Deposit
curl -X POST http://localhost:8080/wallet/deposit?userId=$USER_ID \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000.00}'

# Withdraw
curl -X POST http://localhost:8080/wallet/withdraw?userId=$USER_ID \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00}'

# Check balance
curl http://localhost:8080/wallet/balance?userId=$USER_ID

# View history
curl http://localhost:8080/wallet/transactions?userId=$USER_ID
```

### Scenario 2: Transfer Between Users

```bash
# Get alice's ID (from login response)
ALICE_ID=...

# Get bob's ID (or use the pre-loaded one)
BOB_ID=...

# Alice transfers to bob
curl -X POST "http://localhost:8080/wallet/transfer?fromUserId=$ALICE_ID" \
  -H "Content-Type: application/json" \
  -d '{"toUsername": "bob", "amount": 250.00}'

# Check both balances
curl http://localhost:8080/wallet/balance?userId=$ALICE_ID
curl http://localhost:8080/wallet/balance?userId=$BOB_ID
```

---

## ❌ Common Errors and Solutions

### Error: "Username already exists"
- Register with a different username

### Error: "Invalid username or password"
- Check your credentials
- Make sure username and password match during login

### Error: "Insufficient balance"
- Try depositing more money first
- Check your current balance

### Error: "Recipient not found"
- Double-check the recipient's username
- The recipient must be a registered user

### Error: "Cannot transfer to yourself"
- Use a different recipient username

---

## 📊 Project Structure Summary

```
Controller Layer
├── AuthController
│   ├── POST /auth/register
│   └── POST /auth/login
└── WalletController
    ├── GET /wallet/balance
    ├── POST /wallet/deposit
    ├── POST /wallet/withdraw
    ├── POST /wallet/transfer
    └── GET /wallet/transactions

Service Layer
├── AuthService
└── WalletService

Data Access Layer
├── DataApi (Interface)
└── MockDataApiImpl (Implementation)

Models
├── User
├── Wallet
└── Transaction
```

---

## 🔑 Key Design Patterns Used

1. **Layered Architecture**
   - Clear separation of concerns
   - Each layer has a specific responsibility

2. **Dependency Injection**
   - Spring manages bean dependencies
   - Services injected into controllers

3. **Interface Abstraction**
   - DataApi interface hides implementation details
   - Easy to swap implementations

4. **Data Transfer Objects (DTOs)**
   - Request/Response objects for API
   - Validation handled at controller level

5. **Global Exception Handler**
   - Centralized error handling
   - Consistent error responses

---

## 📚 For More Details

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API documentation with all endpoints and examples.

---

**Happy coding! 🎉**
