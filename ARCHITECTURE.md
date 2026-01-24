# 🏗️ Architecture Documentation

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (HTTP)                             │
└────────────────────────────┬────────────────────────────────────┘
                             │
              ┌──────────────▼──────────────┐
              │   Spring Boot Application   │
              └──────────────┬──────────────┘
                             │
    ┌────────────────────────┼────────────────────────┐
    │                        │                        │
┌───▼──────────┐  ┌─────────▼────────┐  ┌────────────▼────┐
│ Auth Module  │  │ Wallet Module    │  │ Transaction Mgmt│
└───┬──────────┘  └──────┬──────────┘  └────┬──────────────┘
    │                    │                   │
    └────────┬───────────┼───────────┬───────┘
             │           │           │
         ┌───▼───────────▼───────────▼───┐
         │    Service Layer (Business)   │
         │ • AuthService                 │
         │ • WalletService               │
         └───┬─────────────────────────────┘
             │
    ┌────────▼──────────────────────┐
    │   DataApi Interface Layer      │
    │   (Abstraction)                │
    └────────┬──────────────┬────────┘
             │              │
    ┌────────▼────┐   ┌─────▼────────────┐
    │MockDataApiImpl│   │JpaDataApiImpl     │
    │  (Mock impl) │   │(DB impl - Future)│
    └────────┬────┘   └──────────────────┘
             │
    ┌────────▼────────────────┐
    │  MockDataStore          │
    │  (In-Memory Collections)│
    └─────────────────────────┘
```

---

## Component Details

### 1. Controller Layer (`controller/`)

**Responsibility:** Handle HTTP requests and responses

#### AuthController
```java
POST /auth/register    → Register new user
POST /auth/login       → Login existing user
```

#### WalletController
```java
GET  /wallet/balance        → Get user's balance
POST /wallet/deposit        → Deposit money
POST /wallet/withdraw       → Withdraw money
POST /wallet/transfer       → Transfer to another user
GET  /wallet/transactions   → Get transaction history
```

**Key Points:**
- Only accepts valid DTOs
- Validates input using Jakarta validation
- Calls Service layer only
- Returns consistent ApiResponse format
- Does NOT contain business logic

---

### 2. Service Layer (`service/`)

**Responsibility:** Implement business logic and validation

#### AuthService
- **`register(username, password)`**
  - Validates username and password
  - Checks if user already exists
  - Creates new user via DataApi
  - Creates wallet with balance 0
  
- **`login(username, password)`**
  - Validates credentials
  - Finds user in database
  - Checks password match

#### WalletService
- **`getBalance(userId)`**
  - Validates user exists
  - Returns current balance

- **`deposit(amount)`**
  - Validates amount > 0
  - Updates balance
  - Creates transaction record

- **`withdraw(amount)`**
  - Validates amount > 0
  - Checks sufficient balance
  - Updates balance
  - Creates transaction record

- **`transfer(fromUserId, toUsername, amount)`**
  - Validates amount > 0
  - Checks sender balance
  - Validates recipient exists
  - Prevents self-transfer
  - Updates both balances
  - Creates transaction record

- **`getTransactionHistory(userId)`**
  - Retrieves all transactions for user

**Key Points:**
- Contains all business logic
- Depends ONLY on DataApi interface
- Does NOT depend on concrete implementation
- Easy to unit test
- Easy to integrate with database

---

### 3. Data Access Layer (`dataaccess/`)

**Responsibility:** Abstract data operations

#### DataApi Interface
```java
// User operations
User createUser(String username, String password)
User findUserByUsername(String username)
User findUserById(String userId)
boolean userExists(String username)

// Wallet operations
Wallet getWalletByUserId(String userId)
void updateWalletBalance(String userId, Double newBalance)
void createWallet(String userId, Double initialBalance)

// Transaction operations
Transaction createTransaction(...)
List<Transaction> getTransactionsByUserId(String userId)
```

#### MockDataApiImpl (Implementation)
- Implements DataApi interface
- Uses MockDataStore for storage
- Spring @Component annotation
- Initializes with mock data

**Key Points:**
- Contracts defined by interface
- Multiple implementations possible
- Easy to test with mock
- Easy to replace with JPA implementation

---

### 4. Data Storage Layer (`datastore/`)

#### MockDataStore
**Not a Spring component** - plain Java class

Stores:
```java
List<User> users              // All users
List<Wallet> wallets          // All wallets
List<Transaction> transactions // All transactions
```

Methods:
- User CRUD operations
- Wallet operations
- Transaction operations
- Data retrieval/filtering

**Key Points:**
- Uses Java collections only
- No ORM or database
- Perfect for development/testing
- Easy to understand

---

### 5. Model Layer (`model/`)

#### User
```java
String id              // UUID
String username        // Unique
String password        // Plain text (mock only)
```

#### Wallet
```java
String userId          // Foreign key to User
Double balance         // Current balance
```

#### Transaction
```java
String id              // UUID
TransactionType type   // DEPOSIT, WITHDRAW, TRANSFER
String fromUserId      // Sender (null for deposit)
String toUserId        // Recipient (null for withdraw/deposit)
Double amount          // Transaction amount
LocalDateTime createdAt // Timestamp
```

---

### 6. DTO Layer (`dto/`)

**Request DTOs** (`request/`)
- RegisterRequest
- LoginRequest
- DepositRequest
- WithdrawRequest
- TransferRequest

**Response DTOs** (`response/`)
- AuthResponse
- BalanceResponse
- TransactionResponse
- ApiResponse (wrapper)

**Key Points:**
- Input validation via annotations
- Request/Response separation
- Type-safe data transfer

---

### 7. Exception Handling (`exception/`)

#### AppException
Custom runtime exception for application errors

#### GlobalExceptionHandler
Centralized exception handling
- Catches AppException
- Catches validation exceptions
- Catches general exceptions
- Returns consistent error format

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

### 8. Utilities (`util/`)

#### IdGenerator
```java
static String generateId()  // Returns UUID v4
```

---

## Data Flow Examples

### Example 1: User Registration

```
┌─────────────────────────────────────────┐
│ Client sends POST /auth/register        │
├─────────────────────────────────────────┤
│ {username: "john", password: "pass123"} │
└──────────────────┬──────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ AuthController       │
        │ • Receives request   │
        │ • Validates DTO      │
        │ • Calls AuthService  │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ AuthService          │
        │ • Validates input    │
        │ • Checks duplicate   │
        │ • Calls DataApi      │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ MockDataApiImpl       │
        │ • Creates user       │
        │ • Creates wallet     │
        │ • Returns user       │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ MockDataStore        │
        │ • Adds to users[]    │
        │ • Adds to wallets[]  │
        │ • Stores data        │
        └──────────────────────┘
```

### Example 2: Transfer Money

```
┌──────────────────────────────────────────┐
│ Client: POST /wallet/transfer            │
│ ?fromUserId=alice-id                     │
│ {toUsername: "bob", amount: 100}         │
└──────────────────┬───────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ WalletController     │
        │ • Validates request  │
        │ • Extracts params    │
        │ • Calls WalletService│
        └──────────┬───────────┘
                   │
                   ▼
        ┌────────────────────────────┐
        │ WalletService              │
        │ • Validates amount > 0     │
        │ • Gets Alice's wallet      │
        │ • Checks balance           │
        │ • Finds Bob by username    │
        │ • Gets Bob's wallet        │
        │ • Prevents self-transfer   │
        │ • Updates both balances    │
        │ • Creates transaction      │
        └──────────┬─────────────────┘
                   │
          ┌────────┴────────┐
          │                 │
          ▼                 ▼
    ┌─────────────┐    ┌─────────────┐
    │ DataApi     │    │ DataApi     │
    │ (update     │    │ (update     │
    │ Alice)      │    │ Bob)        │
    └────┬────────┘    └────┬────────┘
         │                  │
         ▼                  ▼
    ┌──────────────────────────────┐
    │ MockDataApiImpl               │
    │ • updateWalletBalance(...)   │
    │ • createTransaction(...)      │
    └────┬───────────────────────────┘
         │
         ▼
    ┌──────────────────────────────┐
    │ MockDataStore                │
    │ • wallets[] updated          │
    │ • transactions[] added       │
    └──────────────────────────────┘
```

---

## Why This Architecture?

### 1. **Separation of Concerns**
- Each layer has ONE responsibility
- Easy to understand and maintain
- Easy to test in isolation

### 2. **Dependency Inversion**
- Services depend on interfaces, not implementations
- Controllers depend on services, not data sources
- Easy to swap implementations

### 3. **Testability**
- Mock DataApi for unit tests
- No database needed for testing
- Fast, isolated tests

### 4. **Scalability**
- Easy to add new features
- Easy to split into microservices later
- Easy to add caching layer

### 5. **Reusability**
- Services can be reused by multiple controllers
- DataApi can have multiple implementations
- Models reused across layers

---

## Migration Path: Mock → Database

### Current State (Mock)
```
Controller → Service → DataApi → MockDataApiImpl → MockDataStore
```

### Step 1: Add JPA Support
- Add Spring Data JPA dependency
- Create JpaDataApiImpl
- Create entity classes

### Step 2: Create Repositories
```java
@Repository
interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
```

### Step 3: Implement JpaDataApiImpl
```java
@Component
@ConditionalOnProperty(name = "app.data.source", havingValue = "jpa")
public class JpaDataApiImpl implements DataApi {
    // Implementation using repositories
}
```

### Step 4: Keep MockDataApiImpl
```java
@Component
@ConditionalOnProperty(name = "app.data.source", havingValue = "mock", matchIfMissing = true)
public class MockDataApiImpl implements DataApi {
    // Keep as default for development
}
```

### Result
```
Control via application.properties:
app.data.source=mock      → Use MockDataApiImpl
app.data.source=jpa       → Use JpaDataApiImpl
```

**No changes needed** in Controller or Service layers!

---

## Code Organization Principles

### DRY (Don't Repeat Yourself)
- Common logic in AuthService and WalletService
- Shared DTOs for responses
- Global exception handler

### KISS (Keep It Simple)
- Clear naming conventions
- No unnecessary complexity
- Straightforward data flow

### YAGNI (You Ain't Gonna Need It)
- Only implemented required features
- No premature optimization
- No unused code

### Single Responsibility
- Controllers handle HTTP only
- Services handle business logic
- DataApi handles data access

### Early Return
- Validates and fails fast
- Reduces nested conditions
- Improves readability

---

## Testing Strategy

### Unit Testing
```java
@Test
public void testTransferValidation() {
    // Mock DataApi
    DataApi mockDataApi = mock(DataApi.class);
    WalletService service = new WalletService(mockDataApi);
    
    // Test business logic in isolation
}
```

### Integration Testing
```java
@SpringBootTest
public class WalletControllerIT {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testTransferEndpoint() {
        // Full request-response cycle
    }
}
```

### Benefits of Architecture
- Minimal mock setup required
- Business logic tested without dependencies
- Easy to test each layer independently

---

**This architecture is designed for clarity, maintainability, and future evolution! 🚀**
