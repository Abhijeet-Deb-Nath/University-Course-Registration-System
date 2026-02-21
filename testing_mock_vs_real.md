# Mock vs Real Database - Visual Guide

## 🎯 The Core Question: When to Use Mock vs Real Database?

---

## 📊 **Unit Tests - Everything Mocked**

### **What Gets Mocked:**
```
┌─────────────────────────────────────┐
│      UserService (REAL)             │
│  ┌───────────────────────────┐     │
│  │ Business Logic Being      │     │
│  │ Tested                    │     │
│  └───────────────────────────┘     │
│            ↓        ↓               │
│   ┌────────────┐  ┌──────────────┐ │
│   │ UserRepo   │  │ PasswordEnc  │ │
│   │  (MOCK)    │  │   (MOCK)     │ │
│   └────────────┘  └──────────────┘ │
└─────────────────────────────────────┘
         ❌ No Database
         ❌ No Spring Context
         ✅ Super Fast
```

### **Code Example:**
```java
@ExtendWith(MockitoExtension.class)  // ← Mockito, not Spring
class UserServiceTest {
    
    @Mock  // ❌ FAKE - Mockito creates fake object
    private UserRepository userRepository;
    
    @Mock  // ❌ FAKE - Mockito creates fake object
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks  // ✅ REAL - but uses fake dependencies above
    private UserService userService;
    
    @Test
    void register_WhenUsernameIsUnique_ShouldCreateUser() {
        // Setup fake behavior
        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        
        // Call the real service method
        User user = userService.register(request);
        
        // Verify it called the mocks
        verify(userRepository).existsByUsername("test");
        verify(userRepository).save(any(User.class));
    }
}
```

### **What This Tests:**
✅ Service makes correct decisions  
✅ Service calls repository with right parameters  
✅ Business logic works (duplicate check, etc.)  

### **What This DOESN'T Test:**
❌ Does database actually save?  
❌ Do SQL queries work?  
❌ Do database constraints work?  
❌ Does Spring configuration work?  

---

## 🏗️ **Integration Tests - Everything Real**

### **What's Real:**
```
┌──────────────────────────────────────────────────┐
│         Spring Boot Context (REAL)               │
│  ┌─────────────────────────────────────────┐    │
│  │  MockMvc (Simulates HTTP)               │    │
│  │         ↓                                │    │
│  │  AuthController (REAL)                  │    │
│  │         ↓                                │    │
│  │  Spring Security (REAL)                 │    │
│  │         ↓                                │    │
│  │  UserService (REAL)                     │    │
│  │         ↓                                │    │
│  │  PasswordEncoder (REAL BCrypt)          │    │
│  │         ↓                                │    │
│  │  UserRepository (REAL Spring Data JPA)  │    │
│  │         ↓                                │    │
│  │  H2 Database (REAL in-memory database)  │    │
│  │         ↓                                │    │
│  │  [ id | username | password_hash ]      │    │
│  └─────────────────────────────────────────┘    │
└──────────────────────────────────────────────────┘
         ✅ Real Database (H2)
         ✅ Real Spring Context
         ✅ Real SQL Execution
         ⏱️ Slower (but thorough)
```

### **Code Example:**
```java
@SpringBootTest  // ← Loads FULL Spring context
@Transactional   // ← Real database transactions
class AuthControllerIntegrationTest {
    
    @Autowired  // ✅ REAL Spring bean
    private UserRepository userRepository;
    
    @Autowired  // ✅ REAL Spring Security
    private PasswordEncoder passwordEncoder;
    
    private MockMvc mockMvc;
    
    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        // Make REAL HTTP request
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"pass\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("test"));
        
        // Query REAL database
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        
        // Verify password was REALLY hashed
        String hash = users.get(0).getPasswordHash();
        assertThat(hash).startsWith("$2a$");  // BCrypt format
        assertThat(passwordEncoder.matches("pass", hash)).isTrue();
    }
}
```

### **What This Tests:**
✅ HTTP request parsing (Spring MVC)  
✅ JWT authentication (Spring Security)  
✅ Service business logic  
✅ **Password is ACTUALLY hashed** (not just mocked)  
✅ **User is ACTUALLY saved to database**  
✅ **SQL INSERT query works**  
✅ **Database constraints enforced** (e.g., unique username)  
✅ **Transaction commits successfully**  
✅ JSON response is correct  

### **What You Can't Test With Mocks:**
❌ SQL syntax errors  
❌ Database unique constraints  
❌ Foreign key relationships  
❌ Cascade deletes  
❌ Transaction rollback on error  

---

## 🔬 **Side-by-Side: Same Feature, Two Approaches**

### **Scenario: Duplicate Username Prevention**

#### **Unit Test (Mocked):**
```java
@Test
void register_WhenUsernameExists_ShouldThrowException() {
    // MOCK: Pretend database says username exists
    when(userRepository.existsByUsername("duplicate"))
        .thenReturn(true);  // ← We're TELLING it what to return
    
    // Test: Does service check?
    assertThatThrownBy(() -> userService.register(request))
        .hasMessageContaining("Username already exists");
}
```
**Tests:** Service logic checks for duplicates ✅  
**Doesn't Test:** Database constraint actually works ❌

---

#### **Integration Test (Real Database):**
```java
@Test
void register_WithDuplicateUsername_ShouldReturnConflict() throws Exception {
    // Create REAL user in REAL H2 database
    User existing = new User();
    existing.setUsername("duplicate");
    existing.setPasswordHash("hashed");
    userRepository.save(existing);  // ← REAL database INSERT
    
    // Try to register duplicate via HTTP
    mockMvc.perform(post("/api/auth/register")
            .content("{\"username\":\"duplicate\",\"password\":\"pass\"}"))
        .andExpect(status().isConflict());
    
    // Verify only 1 user in database (duplicate was rejected)
    assertThat(userRepository.count()).isEqualTo(1);
}
```
**Tests:**
- Service logic checks ✅
- Database unique constraint enforced ✅  
- HTTP returns correct error ✅
- Transaction rolled back ✅

---

## 📈 **When to Use Which:**

| Scenario | Unit Test (Mock) | Integration Test (Real) |
|----------|------------------|------------------------|
| Test business logic decision | ✅ | ❌ |
| Test method parameters correct | ✅ | ❌ |
| Test SQL query works | ❌ | ✅ |
| Test database constraints | ❌ | ✅ |
| Test relationships (foreign keys) | ❌ | ✅ |
| Test cascade deletes | ❌ | ✅ |
| Test transaction rollback | ❌ | ✅ |
| Test Spring Security integration | ❌ | ✅ |
| Test full HTTP flow | ❌ | ✅ |
| Fast execution | ✅ | ❌ |

---

## 🎯 **Your Project: What Uses What**

### **Unit Tests (Mock)** - 24 tests
```
src/test/java/service/
├── UserServiceTest.java         ← Mocks UserRepository, PasswordEncoder
├── CourseServiceTest.java       ← Mocks CourseRepository, UserService
└── RegistrationServiceTest.java ← Mocks RegistrationRepository, CourseService
```

**All dependencies are @Mock**  
**No real database**  
**No Spring context**

---

### **Integration Tests (Real)** - 21 tests
```
src/test/java/controller/
├── AuthControllerIntegrationTest.java         ← Real H2, Spring Security
├── CourseControllerIntegrationTest.java       ← Real H2, JWT auth
└── RegistrationControllerIntegrationTest.java ← Real H2, transactions

src/test/java/repository/
└── RegistrationRepositoryIntegrationTest.java ← Real H2, JPA queries
```

**All dependencies are @Autowired (real)**  
**H2 in-memory database (real SQL)**  
**Full Spring context loaded**

---

## 🏭 **H2 Database: How It Works**

### **Configuration:**
```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### **Lifecycle:**
```
Test Starts
    ↓
H2 creates in-memory database
    ↓
Hibernate creates tables (DDL)
    ↓
Test executes (INSERT, SELECT, etc.)
    ↓
@Transactional rolls back changes
    ↓
Next test starts with clean database
    ↓
All tests finish
    ↓
H2 database destroyed (memory freed)
```

### **Benefits:**
✅ **Fast** - Runs in memory (no disk I/O)  
✅ **Isolated** - Each test gets fresh database  
✅ **Real** - Executes actual SQL queries  
✅ **Safe** - Never touches production database  
✅ **Automatic cleanup** - Auto-destroyed after tests  

---

## 💡 **Common Misconceptions Clarified**

### ❌ "Integration tests use mocks too"
**NO!** Integration tests use **@Autowired** (real beans), not **@Mock** (fake objects).

### ❌ "H2 is a mock database"
**NO!** H2 is a **real database** (like PostgreSQL). It just runs in memory instead of disk.

### ❌ "We should only use mocks to be fast"
**NO!** You NEED real database tests to validate SQL, constraints, and transactions.

### ❌ "Integration tests are optional"
**NO!** They're REQUIRED in professional development. Can't deploy without them.

---

## ✅ **Summary: Your Answer**

### **Q: Are you testing everything with mock?**
**A: NO!**
- **Unit tests (24)** → Everything mocked (fake database)
- **Integration tests (21)** → Everything real (H2 database)

### **Q: Is there any need of integrating real database?**
**A: ABSOLUTELY YES!**

**You MUST use real database to test:**
1. SQL queries are correct
2. Database constraints work (unique, foreign keys)
3. JPA relationships work (cascade, eager/lazy loading)
4. Transactions commit/rollback properly
5. Spring Data JPA generates correct SQL
6. Full system integration (HTTP → DB)

**Without real database testing:**
- ❌ Can't verify SQL syntax
- ❌ Can't validate constraints
- ❌ Can't test relationships
- ❌ Can't verify transactions
- ❌ Code might work in tests but fail in production!

---

## 🎓 **What to Tell Your Professor**

**"I implemented two types of testing:**

1. **Unit Tests (24)** - Test business logic in isolation using Mockito mocks
   - Fast, focused, no database needed
   - Validates service layer decisions and error handling

2. **Integration Tests (21)** - Test full system with H2 in-memory database
   - Real database validates SQL queries and constraints
   - Tests authentication, authorization, transactions
   - Ensures components work together correctly

**Both are essential** - Mocks test logic, real database tests persistence and integration. This is industry standard practice."

---

## 📚 **Further Reading**

- Unit tests prove **"Does my logic work?"**
- Integration tests prove **"Does my system work?"**
- Both are needed for production-quality code
- H2 is standard practice for Spring Boot testing
- Professional projects use 70% unit tests (mocked) + 30% integration tests (real DB)
