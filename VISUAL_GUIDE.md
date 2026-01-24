# 🎨 Visual Reference Guide

## API Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     HTTP CLIENT REQUEST                          │
│                    POST /wallet/transfer                         │
│                   body: {toUsername, amount}                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    VALIDATION LAYER
                             ↓
                ┌────────────────────────┐
                │ Request Validation     │
                │ • @Valid on DTO        │
                │ • @NotBlank, @Positive│
                │ • Jakarta Validation   │
                └────────────┬───────────┘
                             │
              ┌──────────────▼──────────────┐
              │   WalletController         │
              │  /wallet/transfer          │
              │ • Receive request          │
              │ • Validate DTO             │
              │ • Call WalletService       │
              │ • Return response          │
              └────────────┬────────────────┘
                           │
           ┌───────────────▼───────────────┐
           │    BUSINESS LOGIC LAYER      │
           │                              │
           │    WalletService.transfer()  │
           │ ✓ Validate amount > 0        │
           │ ✓ Check sender balance       │
           │ ✓ Find recipient             │
           │ ✓ Prevent self-transfer      │
           │ ✓ Get recipient wallet       │
           │ ✓ Update both balances       │
           │ ✓ Record transaction         │
           └────────────┬─────────────────┘
                        │
        ┌───────────────▼────────────────┐
        │    DATA ACCESS LAYER           │
        │                                │
        │    DataApi (Interface)         │
        │ • updateWalletBalance()        │
        │ • createTransaction()          │
        │ • getWalletByUserId()          │
        │ • findUserByUsername()         │
        └────────┬───────────────────────┘
                 │
    ┌────────────▼────────────┐
    │  MockDataApiImpl         │
    │  (Implementation)       │
    │ • Execute data ops      │
    │ • Delegate to store     │
    └────────┬─────────────────┘
             │
    ┌────────▼─────────────┐
    │  MockDataStore       │
    │ • Update wallets[]   │
    │ • Add transactions[] │
    └────────┬──────────────┘
             │
    ┌────────▼────────────────────┐
    │   RESPONSE BUILDING          │
    │                              │
    │ 1. Get updated wallet        │
    │ 2. Create BalanceResponse    │
    │ 3. Wrap in ApiResponse       │
    │ 4. Build ResponseEntity      │
    └────────┬─────────────────────┘
             │
┌────────────▼─────────────────────────────────────┐
│         HTTP JSON RESPONSE (200 OK)              │
│  {                                               │
│    "success": true,                             │
│    "message": "Transfer successful",            │
│    "data": {                                     │
│      "userId": "...",                          │
│      "balance": 750.00                         │
│    }                                             │
│  }                                               │
└──────────────────────────────────────────────────┘
```

---

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLLERS                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ AuthController   │  │WalletController  │                │
│  │                  │  │                  │                │
│  │ /auth/register   │  │ /wallet/balance  │                │
│  │ /auth/login      │  │ /wallet/deposit  │                │
│  │                  │  │ /wallet/withdraw │                │
│  │                  │  │ /wallet/transfer │                │
│  │                  │  │ /wallet/transactions              │
│  └──────┬───────────┘  └───────┬──────────┘                │
└─────────┼──────────────────────┼──────────────────────────┘
          │                      │
          ├──────────┬───────────┤
          │          │           │
    ┌─────▼────┐ ┌──▼──────┐    │
    │ AuthServ │ │WalletServ    │
    │---------│ │-----------   │
    │register │ │getBalance    │
    │login    │ │deposit       │
    │         │ │withdraw      │
    │         │ │transfer      │
    │         │ │getTransactions
    └────┬────┘ └──┬──────────┘
         │         │
         └────┬────┘
              │
    ┌─────────▼──────────┐
    │  DataApi           │
    │  (Interface)       │
    │──────────────────  │
    │createUser()        │
    │findUserByUsername()│
    │getWalletByUserId() │
    │updateWalletBalance│
    │createTransaction()│
    │getTransactionsByUI│
    └────────┬──────────┘
             │
    ┌────────▼──────────┐
    │ MockDataApiImpl    │
    │ (Implementation)  │
    │───────────────── │
    │ ✓ implements all │
    │   DataApi methods│
    └────────┬─────────┘
             │
    ┌────────▼──────────┐
    │ MockDataStore     │
    │ (In-memory DB)    │
    │───────────────── │
    │ users[]           │
    │ wallets[]         │
    │ transactions[]    │
    └───────────────────┘
```

---

## Data Model Relationships

```
┌─────────────────┐
│     USER        │
│─────────────────│
│ id (PK)         │
│ username (UQ)   │
│ password        │
└────────┬────────┘
         │ 1:1
         │
         ├──────────────┐
         │              │
    ┌────▼──────────┐   │
    │   WALLET      │   │
    │──────────────│   │
    │ userId (FK)  │   │
    │ balance      │   │
    └──────────────┘   │
                       │
                   ┌───▼─────────────────┐
                   │  TRANSACTION        │
                   │─────────────────────│
                   │ id (PK)             │
                   │ type (ENUM)         │
                   │ fromUserId (FK)     │ → to WALLET
                   │ toUserId (FK)       │ → to WALLET
                   │ amount              │
                   │ createdAt           │
                   └─────────────────────┘
```

---

## Request/Response Examples in Flow

```
1. REGISTER REQUEST
┌─────────────────────────────────────┐
│ POST /auth/register                 │
│ Content-Type: application/json      │
├─────────────────────────────────────┤
│ {                                   │
│   "username": "john",              │
│   "password": "pass123"            │
│ }                                   │
└─────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────┐
│ REGISTER RESPONSE (201 Created)     │
├─────────────────────────────────────┤
│ {                                   │
│   "success": true,                 │
│   "message": "User registered...",  │
│   "data": {                         │
│     "id": "uuid-1234",             │
│     "username": "john",            │
│     "message": "Registration..."   │
│   }                                 │
│ }                                   │
└─────────────────────────────────────┘

2. TRANSFER REQUEST
┌──────────────────────────────────────┐
│ POST /wallet/transfer?fromUserId=...│
│ Content-Type: application/json      │
├──────────────────────────────────────┤
│ {                                    │
│   "toUsername": "bob",              │
│   "amount": 250.00                  │
│ }                                    │
└──────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────┐
│ TRANSFER RESPONSE (200 OK)          │
├──────────────────────────────────────┤
│ {                                    │
│   "success": true,                  │
│   "message": "Transfer successful..│
│   "data": {                          │
│     "userId": "uuid-1234",          │
│     "balance": 750.00               │
│   }                                  │
│ }                                    │
└──────────────────────────────────────┘

3. ERROR RESPONSE
┌──────────────────────────────────────┐
│ POST /wallet/withdraw?...            │
│ {"amount": 50000.00}                │
├──────────────────────────────────────┤
│ RESPONSE (400 Bad Request)          │
├──────────────────────────────────────┤
│ {                                    │
│   "success": false,                 │
│   "message": "Insufficient balance..│
│   "data": null                       │
│ }                                    │
└──────────────────────────────────────┘
```

---

## Folder Structure Tree

```
demo/
│
├── 📄 pom.xml                           [Maven config]
├── 📄 README.md                         [Main docs]
├── 📄 API_DOCUMENTATION.md              [API reference]
├── 📄 QUICKSTART.md                     [Quick start]
├── 📄 ARCHITECTURE.md                   [Architecture]
├── 📄 CODE_EXAMPLES.md                  [Code examples]
├── 📄 PROJECT_DELIVERY.md               [Delivery summary]
│
└── src/main/java/com/example/demo/
    │
    ├── 🎯 ProjectGaugeApplication.java  [Main Spring class]
    │
    ├── 📁 controller/                   [HTTP Endpoints]
    │   ├── AuthController.java          [/auth/...]
    │   └── WalletController.java        [/wallet/...]
    │
    ├── 📁 service/                      [Business Logic]
    │   ├── AuthService.java             [Auth logic]
    │   └── WalletService.java           [Wallet logic]
    │
    ├── 📁 dataaccess/                   [Data Abstraction]
    │   ├── DataApi.java                 [Interface]
    │   └── MockDataApiImpl.java          [Mock impl]
    │
    ├── 📁 datastore/                    [In-memory DB]
    │   └── MockDataStore.java           [Collections]
    │
    ├── 📁 model/                        [Entities]
    │   ├── User.java                    [User model]
    │   ├── Wallet.java                  [Wallet model]
    │   └── Transaction.java             [Transaction model]
    │
    ├── 📁 dto/                          [DTOs]
    │   ├── request/
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── DepositRequest.java
    │   │   ├── WithdrawRequest.java
    │   │   └── TransferRequest.java
    │   └── response/
    │       ├── AuthResponse.java
    │       ├── BalanceResponse.java
    │       ├── TransactionResponse.java
    │       └── ApiResponse.java
    │
    ├── 📁 exception/                    [Error Handling]
    │   ├── AppException.java            [Custom exception]
    │   └── GlobalExceptionHandler.java  [Global handler]
    │
    └── 📁 util/                         [Utilities]
        └── IdGenerator.java             [UUID generation]
```

---

## Endpoints at a Glance

```
┌─────────┬──────────────────────────┬──────────────────────────┐
│ Method  │ Endpoint                 │ Purpose                  │
├─────────┼──────────────────────────┼──────────────────────────┤
│ POST    │ /auth/register           │ Create new user          │
│ POST    │ /auth/login              │ Authenticate user        │
├─────────┼──────────────────────────┼──────────────────────────┤
│ GET     │ /wallet/balance          │ Get wallet balance       │
│ POST    │ /wallet/deposit          │ Deposit money            │
│ POST    │ /wallet/withdraw         │ Withdraw money           │
│ POST    │ /wallet/transfer         │ Transfer between users   │
│ GET     │ /wallet/transactions     │ Get history              │
└─────────┴──────────────────────────┴──────────────────────────┘
```

---

## Technology Stack Visual

```
┌──────────────────────────────────────────────────┐
│            Spring Boot 4.0.2                     │
├──────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────┐ │
│ │  Spring Web (REST API Support)              │ │
│ │  • DispatcherServlet                        │ │
│ │  • RestController                           │ │
│ │  • RequestMapping                           │ │
│ └─────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────┐ │
│ │  Dependency Injection & Bean Management     │ │
│ │  • @Component, @Service, @Repository        │ │
│ │  • @Autowired                               │ │
│ │  • ApplicationContext                       │ │
│ └─────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────┐ │
│ │  Validation (Jakarta)                       │ │
│ │  • @Valid                                   │ │
│ │  • @NotNull, @NotBlank                      │ │
│ │  • @Positive                                │ │
│ └─────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────┐ │
│ │  Lombok (Code Generation)                   │ │
│ │  • @Data, @Builder                          │ │
│ │  • @AllArgsConstructor                      │ │
│ │  • @NoArgsConstructor                       │ │
│ └─────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
         └─ Java 21 ─┘
```

---

## Migration Path Visual

```
TODAY (Mock Data)              FUTURE (Database)
────────────────              ───────────────

Controller                     Controller
    │                              │
    ▼                              ▼
Service                        Service
    │                              │
    ▼                              ▼
DataApi (Interface)            DataApi (Interface)
    │                              │
    ▼                              ▼
MockDataApiImpl ────────────→  JpaDataApiImpl
    │                              │
    ▼                              ▼
MockDataStore              PostgreSQL/MySQL
    │                          │
    ▼                          ▼
Collections              JPA Repositories
(List<User>,            (UserRepository,
 List<Wallet>,          WalletRepository,
 List<Transaction>)     TransactionRepository)


ADVANTAGE: NO CHANGES NEEDED IN CONTROLLER/SERVICE!
```

---

## State Management Flow

```
┌──────────────────┐
│   CLIENT STATE   │
│──────────────────│
│ User ID          │
│ Username         │
│ Bearer Token     │ (for future JWT)
└────────┬─────────┘
         │
         │ HTTP Request with userId
         ▼
┌──────────────────────────┐
│  SERVER REQUEST STATE    │
│──────────────────────────│
│ @RequestParam userId     │
│ @RequestBody DTO         │
│ Authentication Context   │
└────────┬─────────────────┘
         │
         │ Process
         ▼
┌────────────────────────────────────────┐
│   APPLICATION STATE (MockDataStore)   │
│────────────────────────────────────────│
│ users[] = [User, User, ...]           │
│ wallets[] = [Wallet, Wallet, ...]     │
│ transactions[] = [Tx, Tx, ...]        │
└────────┬───────────────────────────────┘
         │
         │ Build Response
         ▼
┌──────────────────────┐
│  HTTP RESPONSE       │
│──────────────────────│
│ Status Code          │
│ Response Body (JSON) │
│ Headers              │
└──────────────────────┘
```

---

**Visual diagrams created for quick understanding! 📊**
