# 💻 Code Examples & Usage

## Complete Working Examples

### 1. Full User Workflow

```bash
#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== User Registration ===${NC}"
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure_password_123"
  }')

echo $REGISTER_RESPONSE | jq '.'

# Extract user ID (using jq)
USER_ID=$(echo $REGISTER_RESPONSE | jq -r '.data.id')
echo -e "${GREEN}Created user with ID: $USER_ID${NC}"

echo -e "\n${BLUE}=== Check Initial Balance ===${NC}"
curl -s http://localhost:8080/wallet/balance?userId=$USER_ID | jq '.'

echo -e "\n${BLUE}=== Deposit Money ===${NC}"
curl -s -X POST http://localhost:8080/wallet/deposit?userId=$USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00
  }' | jq '.'

echo -e "\n${BLUE}=== Check Balance After Deposit ===${NC}"
curl -s http://localhost:8080/wallet/balance?userId=$USER_ID | jq '.'

echo -e "\n${BLUE}=== Withdraw Money ===${NC}"
curl -s -X POST http://localhost:8080/wallet/withdraw?userId=$USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00
  }' | jq '.'

echo -e "\n${BLUE}=== Transfer to Existing User ===${NC}"
curl -s -X POST http://localhost:8080/wallet/transfer?fromUserId=$USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "toUsername": "alice",
    "amount": 500.00
  }' | jq '.'

echo -e "\n${BLUE}=== View Transaction History ===${NC}"
curl -s http://localhost:8080/wallet/transactions?userId=$USER_ID | jq '.'

echo -e "\n${GREEN}=== Workflow Complete ===${NC}"
```

---

### 2. Error Handling Examples

```bash
#!/bin/bash

echo "=== Test 1: Duplicate Username ==="
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "pass123"
  }' | jq '.'

echo -e "\n=== Test 2: Invalid Credentials ==="
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "wrong_password"
  }' | jq '.'

echo -e "\n=== Test 3: Insufficient Balance ==="
curl -s -X POST http://localhost:8080/wallet/withdraw?userId=some-id \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000.00
  }' | jq '.'

echo -e "\n=== Test 4: Transfer to Self ==="
curl -s -X POST http://localhost:8080/wallet/transfer?fromUserId=some-id \
  -H "Content-Type: application/json" \
  -d '{
    "toUsername": "alice",
    "amount": 100.00
  }' | jq '.'

echo -e "\n=== Test 5: Recipient Not Found ==="
curl -s -X POST http://localhost:8080/wallet/transfer?fromUserId=alice-id \
  -H "Content-Type: application/json" \
  -d '{
    "toUsername": "nonexistent_user",
    "amount": 100.00
  }' | jq '.'
```

---

### 3. Java Integration Examples

#### Using Rest Client (New in Spring Framework 6)

```java
import org.springframework.web.client.RestClient;

@Component
public class WalletApiClient {
    private final RestClient restClient;

    public WalletApiClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8080").build();
    }

    // Register user
    public AuthResponse register(String username, String password) {
        RegisterRequest request = new RegisterRequest(username, password);
        
        return restClient.post()
                .uri("/auth/register")
                .body(request)
                .retrieve()
                .body(ApiResponse.class)
                .getData();
    }

    // Login
    public AuthResponse login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        
        return restClient.post()
                .uri("/auth/login")
                .body(request)
                .retrieve()
                .body(ApiResponse.class)
                .getData();
    }

    // Get balance
    public BalanceResponse getBalance(String userId) {
        return restClient.get()
                .uri("/wallet/balance?userId={userId}", userId)
                .retrieve()
                .body(ApiResponse.class)
                .getData();
    }

    // Deposit
    public BalanceResponse deposit(String userId, Double amount) {
        DepositRequest request = new DepositRequest(amount);
        
        return restClient.post()
                .uri("/wallet/deposit?userId={userId}", userId)
                .body(request)
                .retrieve()
                .body(ApiResponse.class)
                .getData();
    }

    // Transfer
    public BalanceResponse transfer(String fromUserId, String toUsername, Double amount) {
        TransferRequest request = new TransferRequest(toUsername, amount);
        
        return restClient.post()
                .uri("/wallet/transfer?fromUserId={fromUserId}", fromUserId)
                .body(request)
                .retrieve()
                .body(ApiResponse.class)
                .getData();
    }
}
```

#### Using WebTestClient (Reactive Testing)

```java
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletApiTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testCompleteWorkflow() {
        // Register
        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(new RegisterRequest("testuser", "pass123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApiResponse.class)
                .consumeWith(result -> {
                    assert result.getResponseBody().isSuccess();
                });

        // Login
        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(new LoginRequest("testuser", "pass123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> assert response.isSuccess());
    }
}
```

---

### 4. Business Logic Examples

#### AuthService Logic Flow

```java
public class AuthServiceExample {
    
    // Validation flow
    public User register(String username, String password) {
        // 1. Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username cannot be empty");
        }
        
        // 2. Check business rules
        if (dataApi.userExists(username)) {
            throw new AppException("Username already exists");
        }
        
        // 3. Create resource
        User newUser = dataApi.createUser(username, password);
        
        // 4. Setup related resources
        dataApi.createWallet(newUser.getId(), 0.0);
        
        return newUser;
    }
}
```

#### WalletService Logic Flow

```java
public class WalletServiceExample {
    
    // Complex transaction logic
    public Wallet transfer(String fromUserId, String toUsername, Double amount) {
        // 1. Input validation
        if (amount == null || amount <= 0) {
            throw new AppException("Amount must be > 0");
        }
        
        // 2. Business rule validation
        Wallet fromWallet = getBalance(fromUserId);
        if (fromWallet.getBalance() < amount) {
            throw new AppException("Insufficient balance");
        }
        
        // 3. Find related entity
        User toUser = dataApi.findUserByUsername(toUsername);
        if (toUser == null) {
            throw new AppException("Recipient not found");
        }
        
        // 4. Verify business logic
        if (fromUserId.equals(toUser.getId())) {
            throw new AppException("Cannot transfer to yourself");
        }
        
        // 5. Get dependent resource
        Wallet toWallet = dataApi.getWalletByUserId(toUser.getId());
        
        // 6. Update multiple resources (transaction-like)
        dataApi.updateWalletBalance(fromUserId, fromWallet.getBalance() - amount);
        dataApi.updateWalletBalance(toUser.getId(), toWallet.getBalance() + amount);
        
        // 7. Record operation
        dataApi.createTransaction(
            Transaction.TransactionType.TRANSFER,
            fromUserId,
            toUser.getId(),
            amount
        );
        
        return dataApi.getWalletByUserId(fromUserId);
    }
}
```

---

### 5. DataApi Implementation Examples

#### Current: MockDataApiImpl

```java
@Component
public class MockDataApiImpl implements DataApi {
    private final MockDataStore dataStore;

    @Override
    public User createUser(String username, String password) {
        // Simple in-memory implementation
        User user = User.builder()
                .id(IdGenerator.generateId())
                .username(username)
                .password(password)
                .build();
        dataStore.addUser(user);
        return user;
    }
}
```

#### Future: JpaDataApiImpl

```java
@Component
@ConditionalOnProperty(name = "app.data.source", havingValue = "jpa")
public class JpaDataApiImpl implements DataApi {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public User createUser(String username, String password) {
        // Database implementation
        User user = User.builder()
                .id(IdGenerator.generateId())
                .username(username)
                .password(password)
                .build();
        return userRepository.save(user);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ... implement all methods
}
```

---

### 6. Exception Handling Examples

```java
// Example 1: AppException thrown by service
@GetMapping("/balance")
public ResponseEntity<ApiResponse> getBalance(@RequestParam String userId) {
    try {
        Wallet wallet = walletService.getBalance(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Balance retrieved")
                .data(wallet)
                .build());
    } catch (AppException e) {
        // GlobalExceptionHandler catches this
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build());
    }
}

// Example 2: Validation exception
@PostMapping("/register")
public ResponseEntity<ApiResponse> register(
        @Valid @RequestBody RegisterRequest request  // @Valid triggers validation
) {
    // If validation fails, GlobalExceptionHandler catches MethodArgumentNotValidException
    User user = authService.register(request.getUsername(), request.getPassword());
    return ResponseEntity.status(HttpStatus.CREATED).body(...);
}
```

---

### 7. DTO Usage Examples

```java
// Request DTO with validation
@Data
public class TransferRequest {
    @NotBlank(message = "Recipient username is required")
    private String toUsername;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}

// Response DTO
@Data
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
}

// Usage in controller
@PostMapping("/transfer")
public ResponseEntity<ApiResponse> transfer(
        @RequestParam String fromUserId,
        @Valid @RequestBody TransferRequest request  // Automatic validation
) {
    // request.toUsername and request.amount are guaranteed valid
    Wallet result = walletService.transfer(
            fromUserId,
            request.getToUsername(),
            request.getAmount()
    );
    
    return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Transfer successful")
            .data(result)
            .build());
}
```

---

### 8. Model Usage Examples

```java
// Create and use models
User user = User.builder()
        .id(IdGenerator.generateId())
        .username("john")
        .password("secure_pass")
        .build();

Wallet wallet = Wallet.builder()
        .userId(user.getId())
        .balance(1000.0)
        .build();

Transaction transaction = Transaction.builder()
        .id(IdGenerator.generateId())
        .type(Transaction.TransactionType.TRANSFER)
        .fromUserId("user1-id")
        .toUserId("user2-id")
        .amount(500.0)
        .createdAt(LocalDateTime.now())
        .build();

// Lombok provides getters, setters, equals, hashCode, toString
System.out.println(user);  // Readable output thanks to Lombok
```

---

### 9. Testing Examples

```java
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private DataApi dataApi;

    private WalletService walletService;

    @BeforeEach
    public void setup() {
        walletService = new WalletService(dataApi);
    }

    @Test
    public void testDepositSuccess() {
        // Arrange
        String userId = "user-1";
        Wallet wallet = Wallet.builder().userId(userId).balance(100.0).build();
        when(dataApi.getWalletByUserId(userId)).thenReturn(wallet);
        when(dataApi.findUserById(userId)).thenReturn(User.builder().id(userId).build());

        // Act
        Wallet result = walletService.deposit(userId, 50.0);

        // Assert
        assertEquals(150.0, result.getBalance());
        verify(dataApi, times(1)).updateWalletBalance(userId, 150.0);
        verify(dataApi, times(1)).createTransaction(
                Transaction.TransactionType.DEPOSIT,
                userId,
                null,
                50.0
        );
    }

    @Test
    public void testDepositInsufficientAmount() {
        // Arrange
        String userId = "user-1";

        // Act & Assert
        assertThrows(AppException.class, () -> {
            walletService.deposit(userId, -50.0);
        });
    }
}
```

---

## Quick Command Reference

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

### Check Compilation
```bash
mvn compile
```

### Create JAR
```bash
mvn package
```

### Run JAR
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

**These examples demonstrate the real-world usage of the project architecture!**
