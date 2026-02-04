# Authentication & Authorization - Complete Deep Dive

## Table of Contents
1. [Fundamental Concepts](#fundamental-concepts)
2. [Authentication vs Authorization](#authentication-vs-authorization)
3. [How Spring Security Works](#how-spring-security-works)
4. [Password Security](#password-security)
5. [JWT (JSON Web Tokens) Explained](#jwt-json-web-tokens-explained)
6. [The Complete Authentication Flow](#the-complete-authentication-flow)
7. [The Complete Authorization Flow](#the-complete-authorization-flow)
8. [Security Configuration Deep Dive](#security-configuration-deep-dive)
9. [Filter Chain Explained](#filter-chain-explained)
10. [SecurityContextHolder](#securitycontextholder)
11. [Role-Based Access Control](#role-based-access-control)
12. [Attack Prevention](#attack-prevention)
13. [Step-by-Step Example: User Login](#step-by-step-example-user-login)
14. [Step-by-Step Example: Accessing Protected Resource](#step-by-step-example-accessing-protected-resource)
15. [Common Security Pitfalls](#common-security-pitfalls)

---

## Fundamental Concepts

### What is Security in a Web Application?

When you build a web application, you need to answer two critical questions for every request:

1. **Who are you?** (Authentication)
2. **What are you allowed to do?** (Authorization)

Think of it like entering a building:
- **Authentication** = Showing your ID at the entrance to prove who you are
- **Authorization** = Your ID badge determines which rooms you can access

### Why is Security Important?

Without security:
- Anyone could pretend to be you
- Unauthorized users could access sensitive data
- Malicious users could modify or delete data
- Your application would be vulnerable to attacks

---

## Authentication vs Authorization

### Authentication: "Who are you?"

**Definition:** The process of verifying the identity of a user.

**Real-world analogies:**
- Showing your passport at airport security
- Entering your password to unlock your phone
- Scanning your fingerprint to access your laptop

**In your application:**
- User provides username and password
- System verifies credentials against database
- System issues a token (JWT) as proof of identity

**Example:**
```
User: "I am john_doe"
System: "Prove it by providing your password"
User: "Here's my password: secret123"
System: "Correct! Here's your access token: eyJhbG..."
```

### Authorization: "What can you do?"

**Definition:** The process of determining what an authenticated user is allowed to do.

**Real-world analogies:**
- Employee badge allows access to specific floors
- Driver's license type determines what vehicles you can drive
- Credit card limit determines how much you can spend

**In your application:**
- Student can enroll in courses but cannot create them
- Teacher can create courses but cannot enroll as a student
- Admin (if you add this role) can do everything

**Example:**
```
User (john_doe, STUDENT): "I want to create a course"
System: "You are authenticated, but you don't have TEACHER role"
System: "Access denied (403 Forbidden)"

User (prof_smith, TEACHER): "I want to create a course"
System: "You have TEACHER role. Permission granted!"
```

### Key Difference

| Authentication | Authorization |
|----------------|---------------|
| Verifies identity | Verifies permissions |
| Happens first | Happens after authentication |
| Answers "Who are you?" | Answers "What can you do?" |
| Uses credentials (username/password) | Uses roles/permissions |
| Result: Token/Session | Result: Allow/Deny |

---

## How Spring Security Works

### What is Spring Security?

Spring Security is a **framework** that provides:
- Authentication mechanisms
- Authorization rules
- Protection against common attacks
- Integration with various authentication providers

**Think of it as a security guard at every entrance** of your application, checking credentials and permissions before allowing access.

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Security                         │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  1. Filter Chain                                      │ │
│  │     - Intercepts every HTTP request                   │ │
│  │     - Runs security checks                            │ │
│  └───────────────────────────────────────────────────────┘ │
│                            ↓                                │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  2. Authentication Manager                            │ │
│  │     - Validates credentials                           │ │
│  │     - Loads user details                              │ │
│  └───────────────────────────────────────────────────────┘ │
│                            ↓                                │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  3. Security Context                                  │ │
│  │     - Stores current user's authentication            │ │
│  │     - Available throughout request processing         │ │
│  └───────────────────────────────────────────────────────┘ │
│                            ↓                                │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  4. Access Decision Manager                           │ │
│  │     - Checks if user has required permissions         │ │
│  │     - Allows or denies access                         │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## Password Security

### Why Not Store Plain-Text Passwords?

**NEVER do this:**
```sql
CREATE TABLE users (
    username VARCHAR(64),
    password VARCHAR(64)  -- DANGER!
);

INSERT INTO users VALUES ('john_doe', 'secret123');
```

**Why it's dangerous:**
1. **Database breach**: Attacker gets all passwords
2. **Insider threat**: DBAs can see passwords
3. **Logs**: Passwords might appear in logs
4. **Backups**: Password exposed in backups

**Real consequences:**
- Users' passwords stolen
- Same password used elsewhere (email, bank) also compromised
- Legal liability (GDPR violations)
- Reputation damage

### Password Hashing

**Solution:** Store a **one-way hash** of the password.

#### What is Hashing?

**Hashing** is a one-way mathematical function:
```
Input: "secret123"
Function: hash()
Output: "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ..."
```

**Properties:**
- **One-way**: Cannot reverse the hash to get original password
- **Deterministic**: Same input always produces same output
- **Fast to compute**: Quick to generate hash
- **Avalanche effect**: Small change in input drastically changes output

**Example:**
```
hash("secret123") = "$2a$10$N9qo8uLOickgx2ZqFq9jG..."
hash("secret124") = "$2a$10$mK8pL3nM1xYz7Wq4Vr2jH..."  // Completely different!
```

#### BCrypt Algorithm

Your application uses **BCrypt**, which adds:

1. **Salt**: Random data added to password before hashing
2. **Cost factor**: Makes hashing computationally expensive
3. **Adaptive**: Can increase cost as computers get faster

**BCrypt hash structure:**
```
$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ...
│││ │  │                                        │
│││ │  │                                        └─ Hash (31 chars)
│││ │  └─ Salt (22 chars)
│││ └─ Cost factor (10 = 2^10 = 1024 iterations)
││└─ Minor version
│└─ Major version
└─ Algorithm identifier
```

**Why salt matters:**
```
// Without salt (WEAK)
hash("secret123") = "abc123..."  // Always same

User1: password = "secret123" → hash = "abc123..."
User2: password = "secret123" → hash = "abc123..."
// Attacker knows if two users have same password!

// With salt (STRONG)
User1: password = "secret123" + salt1 → hash = "xyz789..."
User2: password = "secret123" + salt2 → hash = "pqr456..."
// Different hashes even for same password!
```

### How Your Application Hashes Passwords

#### Registration: Hashing the Password

```java
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        
        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.password());
        user.setPasswordHash(hashedPassword);
        
        return userRepository.save(user);
    }
}
```

**What happens:**
```
1. User submits: password = "secret123"

2. passwordEncoder.encode("secret123") is called

3. BCrypt algorithm:
   a. Generates random salt: "N9qo8uLOickgx2ZqFq9jG."
   b. Combines password + salt: "secret123N9qo8uLOickgx2ZqFq9jG."
   c. Hashes 2^10 (1024) times
   d. Produces: "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ..."

4. Stored in database: password_hash = "$2a$10$N9qo..."
```

**Database:**
```sql
INSERT INTO users (username, password_hash, role) 
VALUES ('john_doe', '$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ...', 'STUDENT');
```

#### Login: Verifying the Password

```java
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest request) {
        // Spring Security handles password verification
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );
        // If we reach here, password is correct!
        // ...generate JWT token...
    }
}
```

**What happens internally:**

```
1. User submits: username = "john_doe", password = "secret123"

2. Spring Security loads user from database:
   - username = "john_doe"
   - passwordHash = "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4..."

3. passwordEncoder.matches(submitted, stored) is called:
   
   a. Extract salt from stored hash: "N9qo8uLOickgx2ZqFq9jG."
   
   b. Hash submitted password with same salt:
      hash("secret123" + "N9qo8uLOickgx2ZqFq9jG.") 
      = "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4..."
   
   c. Compare:
      Submitted hash: "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4..."
      Stored hash:    "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4..."
      Match? YES!

4. Authentication succeeds
```

**Key insight:** You never decrypt the password. You hash the submitted password and compare hashes!

### PasswordEncoder Bean

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**What this does:**
- Creates a single instance of BCryptPasswordEncoder
- Spring injects this wherever PasswordEncoder is needed
- All password operations use the same encoder

**Cost factor (optional):**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Higher = more secure, slower
}
```

**Recommendation:** Use default (10) unless you have specific security requirements.

---

## JWT (JSON Web Tokens) Explained

### What is JWT?

JWT is a **self-contained token** that carries information about a user.

**Analogy:** It's like a concert wristband:
- Shows you paid for entry (authenticated)
- Shows which areas you can access (VIP, General Admission)
- Cannot be forged (cryptographic signature)
- Expires after the event

### JWT Structure

A JWT consists of **three parts** separated by dots:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

├─────────── Header ──────────┤├───────────────────── Payload ─────────────────────────┤├────── Signature ──────┤
```

#### 1. Header (Base64 encoded)

**Encoded:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
```

**Decoded:**
```json
{
  "alg": "HS256",     // Algorithm: HMAC SHA-256
  "typ": "JWT"        // Type: JWT
}
```

**Purpose:** Tells how the JWT is signed.

#### 2. Payload (Base64 encoded)

**Encoded:**
```
eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9
```

**Decoded:**
```json
{
  "sub": "john_doe",           // Subject: who this token is for
  "role": "STUDENT",           // Custom claim: user's role
  "iat": 1640000000,           // Issued at: timestamp
  "exp": 1640003600            // Expiration: timestamp (1 hour later)
}
```

**Standard claims:**
- `sub` (subject): Username or user ID
- `iat` (issued at): When token was created
- `exp` (expiration): When token becomes invalid
- `iss` (issuer): Who created the token
- `aud` (audience): Who should accept this token

**Custom claims:**
- `role`: User's role
- Any other data you want to include

**Important:** Data is **not encrypted**, only **encoded**. Don't put sensitive data here!

#### 3. Signature

**Purpose:** Ensures the token hasn't been tampered with.

**How it's created:**
```javascript
signature = HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

**Example:**
```
Data to sign: 
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9"

Secret key (from application.properties):
  "change-this-secret-to-a-long-random-string-at-least-32-chars"

Result:
  "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
```

**Verification:**
```
Server receives JWT → Extracts header and payload → 
Re-calculates signature using secret key →
Compares with received signature →
If match: Token is valid ✓
If different: Token was tampered with ✗
```

### Why JWT?

#### Traditional Session-Based Authentication

```
1. User logs in
2. Server creates session, stores in memory/database
3. Server sends session ID cookie to client
4. Client sends cookie with each request
5. Server looks up session in storage
```

**Problems:**
- Server must store sessions (memory/database overhead)
- Difficult to scale (session must be shared across servers)
- Server maintains state

#### JWT-Based Authentication (Your Application)

```
1. User logs in
2. Server generates JWT, signs with secret
3. Server sends JWT to client
4. Client stores JWT (localStorage)
5. Client sends JWT in Authorization header
6. Server validates signature (no database lookup!)
```

**Benefits:**
- **Stateless**: Server doesn't store anything
- **Scalable**: Any server can validate token
- **Self-contained**: Token contains user info
- **Cross-domain**: Can be used across different domains

### How Your Application Uses JWT

#### JwtService: Token Generation

```java
@Service
public class JwtService {
    private final Key signingKey;
    private final long expirationSeconds;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        // Create signing key from secret
        this.signingKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);
        
        return Jwts.builder()
            .setSubject(subject)              // Username
            .addClaims(claims)                // Custom claims (role)
            .setIssuedAt(Date.from(now))      // Current time
            .setExpiration(Date.from(expiration)) // 1 hour from now
            .signWith(signingKey, SignatureAlgorithm.HS256) // Sign
            .compact();                       // Build the JWT string
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)        // Use same key to verify
            .build()
            .parseClaimsJws(token)            // Parse and verify signature
            .getBody();                       // Extract claims
    }
}
```

**Step-by-step token generation:**

```
1. Input:
   subject = "john_doe"
   claims = {"role": "STUDENT"}
   expirationSeconds = 3600

2. Calculate times:
   now = 2026-02-04T10:00:00Z
   expiration = 2026-02-04T11:00:00Z

3. Build payload:
   {
     "sub": "john_doe",
     "role": "STUDENT",
     "iat": 1640000000,
     "exp": 1640003600
   }

4. Create header:
   {
     "alg": "HS256",
     "typ": "JWT"
   }

5. Encode both:
   header_encoded = base64(header)
   payload_encoded = base64(payload)

6. Sign:
   signature = HMACSHA256(header_encoded + "." + payload_encoded, secret_key)

7. Combine:
   jwt = header_encoded + "." + payload_encoded + "." + signature

8. Return:
   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
```

#### Configuration (application.properties)

```properties
app.jwt.secret=change-this-secret-to-a-long-random-string-at-least-32-chars
app.jwt.expiration-seconds=3600
```

**Important:**
- Secret must be **at least 32 characters** for HS256
- Secret must be **kept secret**! Anyone with the secret can forge tokens
- In production, use environment variable, not hardcoded value

#### Token Expiration

**Why tokens expire:**
1. **Stolen token**: If token is stolen, damage is limited to expiration time
2. **Revocation**: User logout wouldn't work with permanent tokens
3. **Security best practice**: Regularly re-authenticate users

**Typical expiration times:**
- Access tokens: 15 minutes - 1 hour
- Refresh tokens: 7-30 days

**Your application:** 1 hour (3600 seconds)

---

## The Complete Authentication Flow

### Overview

```
┌──────────┐                                          ┌──────────┐
│  Client  │                                          │  Server  │
└────┬─────┘                                          └────┬─────┘
     │                                                      │
     │  1. POST /api/auth/login                            │
     │     {username, password}                            │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │                        2. Load user from database   │
     │                           (UserPrincipalService)    │
     │                                                      │
     │                        3. Verify password           │
     │                           (BCrypt comparison)       │
     │                                                      │
     │                        4. Generate JWT token        │
     │                           (JwtService)              │
     │                                                      │
     │  5. Return token                                    │
     │     {token, type, expiresIn}                        │
     │<────────────────────────────────────────────────────┤
     │                                                      │
     │  6. Store token in localStorage                     │
     │                                                      │
     │  7. Include token in subsequent requests            │
     │     Authorization: Bearer <token>                   │
     ├────────────────────────────────────────────────────>│
     │                                                      │
     │                        8. Validate token            │
     │                           (JwtAuthenticationFilter) │
     │                                                      │
     │                        9. Extract username          │
     │                                                      │
     │                       10. Set SecurityContext       │
     │                                                      │
     │  11. Process request                                │
     │      (User is authenticated)                        │
     │                                                      │
```

### Step 1: User Submits Login Request

**Frontend (JavaScript):**
```javascript
async function login(username, password) {
    const response = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
        throw new Error('Login failed');
    }
    
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
}
```

**HTTP Request:**
```
POST /api/auth/login HTTP/1.1
Host: localhost:8081
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secret123"
}
```

### Step 2: Controller Receives Request

**AuthController:**
```java
@PostMapping("/login")
public AuthResponse login(@Valid @RequestBody AuthRequest request) {
    return authService.login(request);
}
```

**What happens:**
1. Spring converts JSON to `AuthRequest` object
2. `@Valid` triggers validation (checks @NotBlank, etc.)
3. Calls `authService.login()`

### Step 3: AuthService Authenticates User

**AuthService.login():**
```java
public AuthResponse login(AuthRequest request) {
    // Step 3.1: Attempt authentication
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),  // "john_doe"
            request.password()   // "secret123"
        )
    );
    
    // If we reach here, authentication succeeded!
    
    // Step 3.2: Load full user details
    User user = userService.findByUsername(authentication.getName())
        .orElseThrow();
    
    // Step 3.3: Generate JWT token
    String token = jwtService.generateToken(
        user.getUsername(),
        Map.of("role", user.getRole().name())
    );
    
    // Step 3.4: Return token
    return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
}
```

### Step 4: AuthenticationManager Validates Credentials

**What happens inside `authenticationManager.authenticate()`:**

```
┌─────────────────────────────────────────────────────────────┐
│ AuthenticationManager.authenticate()                        │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Call UserDetailsService to load user               │
│                                                             │
│ UserPrincipalService.loadUserByUsername("john_doe")        │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 2: Query database                                      │
│                                                             │
│ SELECT * FROM users WHERE username = 'john_doe'            │
│                                                             │
│ Result:                                                     │
│   id = 1                                                    │
│   username = "john_doe"                                     │
│   passwordHash = "$2a$10$N9qo8uLOickgx2ZqFq9jG..."         │
│   role = "STUDENT"                                          │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Create UserDetails object                          │
│                                                             │
│ UserDetails userDetails = new User(                        │
│     "john_doe",                                             │
│     "$2a$10$N9qo8uLOickgx2ZqFq9jG...",                     │
│     [ROLE_STUDENT]                                          │
│ )                                                           │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 4: Verify password                                     │
│                                                             │
│ passwordEncoder.matches(                                    │
│     "secret123",                          // submitted      │
│     "$2a$10$N9qo8uLOickgx2ZqFq9jG..."    // stored         │
│ )                                                           │
│                                                             │
│ Returns: true ✓                                             │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 5: Create Authentication object                       │
│                                                             │
│ Authentication auth = new UsernamePasswordAuthenticationToken(│
│     userDetails,                                            │
│     null,                                                   │
│     [ROLE_STUDENT]                                          │
│ )                                                           │
│ auth.setAuthenticated(true)                                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 6: Return Authentication object                        │
│                                                             │
│ If password matches: return auth                            │
│ If password wrong: throw BadCredentialsException            │
└─────────────────────────────────────────────────────────────┘
```

**UserPrincipalService implementation:**

```java
@Service
public class UserPrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        // Load user from database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Convert to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),                           // Username
            user.getPasswordHash(),                       // Password hash
            List.of(new SimpleGrantedAuthority(          // Authorities (roles)
                "ROLE_" + user.getRole().name()          // "ROLE_STUDENT"
            ))
        );
    }
}
```

**Key points:**
- `UserDetails` is Spring Security's interface for user information
- Must return password hash (not plain text)
- Authorities must be prefixed with `ROLE_`

### Step 5: Generate JWT Token

**JwtService.generateToken():**
```java
public String generateToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();                      // 2026-02-04T10:00:00Z
    Instant expiration = now.plusSeconds(3600);       // 2026-02-04T11:00:00Z
    
    return Jwts.builder()
        .setSubject(subject)                          // "john_doe"
        .addClaims(claims)                            // {"role": "STUDENT"}
        .setIssuedAt(Date.from(now))                  // 1640000000
        .setExpiration(Date.from(expiration))         // 1640003600
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
}
```

**Generated token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Step 6: Return Token to Client

**HTTP Response:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

### Step 7: Client Stores Token

**JavaScript:**
```javascript
const response = await login('john_doe', 'secret123');
localStorage.setItem('token', response.token);

// Now stored in browser's localStorage:
// key: "token"
// value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**localStorage** is browser storage that persists across page reloads.

### Step 8: Client Includes Token in Requests

**JavaScript:**
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:8081/api/courses', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});
```

**HTTP Request:**
```
GET /api/courses HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## The Complete Authorization Flow

Authorization happens **after** authentication. The system knows who you are, now it checks what you can do.

### Step 1: Request Reaches Server

**HTTP Request:**
```
POST /api/courses HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "courseNo": "CS101",
  "courseName": "Introduction to Programming"
}
```

### Step 2: JwtAuthenticationFilter Intercepts

**JwtAuthenticationFilter.doFilterInternal():**

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {
    
    // Step 2.1: Extract token from header
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);  // No token, continue
        return;
    }
    
    String token = header.substring(7);  // Remove "Bearer " prefix
    
    try {
        // Step 2.2: Parse and validate token
        Claims claims = jwtService.parseClaims(token);
        
        // Step 2.3: Extract username
        String username = claims.getSubject();  // "john_doe"
        
        // Step 2.4: Check if already authenticated
        if (username != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Step 2.5: Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Step 2.6: Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()  // [ROLE_STUDENT]
                );
            
            // Step 2.7: Store in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception e) {
        // Token invalid/expired
        SecurityContextHolder.clearContext();
    }
    
    // Step 2.8: Continue filter chain
    filterChain.doFilter(request, response);
}
```

**What happens:**

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Extract Authorization header                             │
│    "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."         │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Parse JWT token                                          │
│    - Verify signature                                       │
│    - Check expiration                                       │
│    - Extract claims                                         │
│                                                             │
│    Claims:                                                  │
│      sub: "john_doe"                                        │
│      role: "STUDENT"                                        │
│      exp: 1640003600                                        │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Load user from database                                  │
│    UserDetailsService.loadUserByUsername("john_doe")        │
│                                                             │
│    Returns:                                                 │
│      username: "john_doe"                                   │
│      authorities: [ROLE_STUDENT]                            │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Create Authentication object                            │
│    - Principal: UserDetails                                 │
│    - Authorities: [ROLE_STUDENT]                            │
│    - Authenticated: true                                    │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Store in SecurityContextHolder                          │
│    SecurityContextHolder.getContext()                       │
│        .setAuthentication(authentication)                   │
│                                                             │
│    Now available throughout request processing!             │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Continue to controller                                   │
└─────────────────────────────────────────────────────────────┘
```

### Step 3: Request Reaches Controller

**CourseController:**
```java
@PostMapping
@PreAuthorize("hasRole('TEACHER')")  // ← Authorization check!
@ResponseStatus(HttpStatus.CREATED)
public CourseResponse create(@Valid @RequestBody CourseRequest request) {
    return toResponse(courseService.createCourse(request));
}
```

### Step 4: @PreAuthorize Checks Role

**What `@PreAuthorize("hasRole('TEACHER')")` does:**

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Get current authentication from SecurityContextHolder    │
│                                                             │
│    Authentication auth = SecurityContextHolder              │
│        .getContext()                                        │
│        .getAuthentication();                                │
│                                                             │
│    auth.getAuthorities() = [ROLE_STUDENT]                  │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Check if authorities contain "ROLE_TEACHER"             │
│                                                             │
│    Required: ROLE_TEACHER                                   │
│    User has: ROLE_STUDENT                                   │
│                                                             │
│    Match? NO ✗                                              │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Throw AccessDeniedException                             │
│                                                             │
│    Spring Security catches this and returns:                │
│    HTTP 403 Forbidden                                       │
└─────────────────────────────────────────────────────────────┘
```

**Result:**
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "timestamp": "2026-02-04T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/courses"
}
```

### Step 5: If Authorized, Execute Method

**If user had ROLE_TEACHER:**

```
┌─────────────────────────────────────────────────────────────┐
│ 1. @PreAuthorize check passes ✓                            │
│    User has ROLE_TEACHER                                    │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Controller method executes                               │
│    create(@RequestBody CourseRequest request)               │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Service method executes                                  │
│    courseService.createCourse(request)                      │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Course created in database                               │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Success response                                         │
│    HTTP 201 Created                                         │
└─────────────────────────────────────────────────────────────┘
```

---

## Security Configuration Deep Dive

### SecurityConfig Class

```java
@Configuration
@EnableMethodSecurity  // Enables @PreAuthorize, @Secured, etc.
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) 
            throws Exception {
        http
            // Disable CSRF (not needed for JWT)
            .csrf(csrf -> csrf.disable())
            
            // Stateless session (no server-side sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/register.html",
                    "/student.html",
                    "/teacher.html",
                    "/styles.css",
                    "/app.js",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                
                // CSS and JS files
                .requestMatchers("/**/*.css", "/**/*.js").permitAll()
                
                // Registration and login endpoints
                .requestMatchers(HttpMethod.POST, 
                    "/api/auth/register", 
                    "/api/auth/login"
                ).permitAll()
                
                // Health check endpoint
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before Spring Security's authentication filter
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

### Configuration Explained Line by Line

#### 1. CSRF Disabled

```java
.csrf(csrf -> csrf.disable())
```

**What is CSRF?**
Cross-Site Request Forgery: An attack where a malicious site tricks your browser into making requests to another site where you're authenticated.

**Example attack:**
```html
<!-- Malicious site -->
<form action="https://bank.com/transfer" method="POST">
    <input name="amount" value="1000000">
    <input name="to" value="attacker">
</form>
<script>document.forms[0].submit();</script>
```

If you're logged into bank.com, your browser will send your session cookie, and the transfer might succeed!

**Why we can disable it:**
- We use JWT tokens in Authorization header
- Not using cookies for authentication
- Browsers don't automatically send Authorization headers

**When you MUST enable it:**
- Using session cookies
- Using cookie-based authentication

#### 2. Stateless Session

```java
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**What this means:**
- Spring Security won't create HTTP sessions
- No session stored on server
- Each request must contain JWT token

**Why stateless?**
- Scalability: No session synchronization across servers
- Simplicity: No session timeout management
- RESTful: Each request is self-contained

#### 3. Authorization Rules

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/", "/index.html", ...).permitAll()
    .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
    .anyRequest().authenticated()
)
```

**Rule evaluation order:**
1. Checks first matching rule
2. If no match, goes to next rule
3. Last rule is catch-all

**Example requests:**

| Request | Matches Rule | Result |
|---------|--------------|--------|
| GET /index.html | `.requestMatchers("/index.html").permitAll()` | Allowed ✓ |
| POST /api/auth/login | `.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()` | Allowed ✓ |
| GET /api/courses | `.anyRequest().authenticated()` | Requires authentication |
| POST /api/courses | `.anyRequest().authenticated()` | Requires authentication |

**Common matchers:**
```java
// Exact path
.requestMatchers("/api/users")

// Path with method
.requestMatchers(HttpMethod.GET, "/api/users")

// Wildcard
.requestMatchers("/api/users/*")      // Matches /api/users/123
.requestMatchers("/api/users/**")     // Matches /api/users/123/profile

// Multiple paths
.requestMatchers("/path1", "/path2", "/path3")

// Pattern
.requestMatchers("/**/*.css")         // All CSS files
```

#### 4. Filter Order

```java
.addFilterBefore(
    jwtAuthenticationFilter, 
    UsernamePasswordAuthenticationFilter.class
)
```

**Spring Security Filter Chain:**
```
Request
  ↓
[CorsFilter]
  ↓
[JwtAuthenticationFilter] ← Your custom filter (added here)
  ↓
[UsernamePasswordAuthenticationFilter] ← Spring's default
  ↓
[ExceptionTranslationFilter]
  ↓
[AuthorizationFilter]
  ↓
Controller
```

**Why before `UsernamePasswordAuthenticationFilter`?**
- That filter expects username/password in request body
- We use JWT tokens in headers
- Our filter populates SecurityContext before Spring's filter runs

---

## Filter Chain Explained

### What is a Filter?

A **filter** is code that runs **before** your controller method.

**Analogy:** Security checkpoint at airport
- Everyone passes through (intercepts all requests)
- Checks credentials
- Decides whether to allow passage

### Filter Interface

```java
public interface Filter {
    void doFilter(
        ServletRequest request, 
        ServletResponse response, 
        FilterChain chain
    ) throws IOException, ServletException;
}
```

**Key concept:** `filterChain.doFilter(request, response)`
- Passes request to next filter
- Like a chain: Filter1 → Filter2 → Filter3 → Controller

### Your JwtAuthenticationFilter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Check for Authorization header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            // No token, continue anyway (might be public endpoint)
            filterChain.doFilter(request, response);
            return;
        }
        
        // 2. Extract token
        String token = header.substring(7);
        
        try {
            // 3. Parse and validate token
            Claims claims = jwtService.parseClaims(token);
            String username = claims.getSubject();
            
            // 4. Authenticate user
            if (username != null && 
                SecurityContextHolder.getContext().getAuthentication() == null) {
                
                UserDetails userDetails = 
                    userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 5. Store in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            // Token invalid, clear context
            SecurityContextHolder.clearContext();
        }
        
        // 6. Continue to next filter
        filterChain.doFilter(request, response);
    }
}
```

### OncePerRequestFilter

**Why extend `OncePerRequestFilter`?**

In complex applications, a request might pass through filters multiple times (e.g., forwarding, includes).

`OncePerRequestFilter` guarantees the filter runs **exactly once per request**.

### Filter Execution Flow

**Request with valid token:**
```
1. JwtAuthenticationFilter.doFilterInternal()
   ├─ Extract token
   ├─ Validate token ✓
   ├─ Set SecurityContext
   └─ Call filterChain.doFilter()
        ↓
2. Other Spring Security filters
        ↓
3. AuthorizationFilter
   ├─ Check @PreAuthorize
   └─ User has ROLE_TEACHER ✓
        ↓
4. DispatcherServlet
        ↓
5. CourseController.create()
```

**Request with invalid token:**
```
1. JwtAuthenticationFilter.doFilterInternal()
   ├─ Extract token
   ├─ Validate token ✗ (expired/invalid signature)
   ├─ Clear SecurityContext
   └─ Call filterChain.doFilter()
        ↓
2. Other Spring Security filters
        ↓
3. AuthorizationFilter
   ├─ Check @PreAuthorize
   └─ No authentication in SecurityContext
        ↓
4. Return 401 Unauthorized
```

**Request to public endpoint:**
```
1. JwtAuthenticationFilter.doFilterInternal()
   ├─ No Authorization header
   └─ Call filterChain.doFilter()
        ↓
2. Other Spring Security filters
        ↓
3. AuthorizationFilter
   ├─ Endpoint is .permitAll()
   └─ Allow access ✓
        ↓
4. DispatcherServlet
        ↓
5. AuthController.login()
```

---

## SecurityContextHolder

### What is SecurityContextHolder?

A **thread-local storage** that holds the security context (authentication information) for the current request.

**Analogy:** A backstage pass at a concert
- You wear it throughout the event
- Security can check it anytime
- Only valid during the event (request)
- Each person (thread) has their own pass

### How It Works

```java
// Set authentication (in filter)
Authentication auth = new UsernamePasswordAuthenticationToken(...);
SecurityContextHolder.getContext().setAuthentication(auth);

// Get authentication (anywhere in your code)
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
```

### Thread-Local Storage

**What is thread-local?**

Each **thread** has its own copy of the variable.

**Why this matters:**
```
Server handles multiple requests concurrently:

Thread 1 (Request A):
  SecurityContextHolder → Authentication for user "john_doe"

Thread 2 (Request B):
  SecurityContextHolder → Authentication for user "jane_doe"

Thread 3 (Request C):
  SecurityContextHolder → No authentication
```

Each request is processed by a separate thread, and SecurityContext is isolated.

### Using SecurityContextHolder in Your Code

**UserService.getCurrentUser():**
```java
public User getCurrentUser() {
    // Get authentication from thread-local storage
    Authentication authentication = SecurityContextHolder
        .getContext()
        .getAuthentication();
    
    if (authentication == null || authentication.getName() == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
    
    // Find user in database
    return userRepository.findByUsername(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, 
            "Unauthorized"
        ));
}
```

**This works because:**
1. Filter set authentication at start of request
2. SecurityContext stored in thread-local
3. Any code in same thread can access it
4. After request completes, SecurityContext is cleared

**Flow:**
```
Request starts (Thread 123)
  ↓
JwtAuthenticationFilter
  SecurityContextHolder.getContext().setAuthentication(auth)
  ↓
Controller
  ↓
Service
  User user = getCurrentUser()
    ↓ SecurityContextHolder.getContext().getAuthentication()
    ↓ Returns authentication set by filter
  ↓
Response sent
  ↓
Spring clears SecurityContext
```

---

## Role-Based Access Control

### Roles in Your Application

```java
public enum Role {
    TEACHER,
    STUDENT
}
```

### How Roles Are Stored

**Database:**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64),
    password_hash VARCHAR(100),
    role VARCHAR(16)  -- "TEACHER" or "STUDENT"
);
```

**Java Entity:**
```java
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private Role role;
}
```

### Converting to Spring Security Authorities

**UserPrincipalService:**
```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                                        // ^^^^^^
                                        // Must prefix with "ROLE_"
    );
}
```

**Why "ROLE_" prefix?**

Spring Security convention:
- Store in database: `STUDENT`
- Store in authority: `ROLE_STUDENT`
- Check with: `hasRole('STUDENT')` (automatically adds ROLE_ prefix)

### Authorization Methods

#### 1. Method-Level Security

**@PreAuthorize:**
```java
@PreAuthorize("hasRole('TEACHER')")
public CourseResponse create(@RequestBody CourseRequest request) {
    // Only users with ROLE_TEACHER can call this
}
```

**@PreAuthorize expressions:**
```java
// Single role
@PreAuthorize("hasRole('TEACHER')")

// Multiple roles (OR)
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")

// Multiple roles (AND)
@PreAuthorize("hasRole('TEACHER') and hasRole('ADMIN')")

// Check authentication
@PreAuthorize("isAuthenticated()")

// Check username
@PreAuthorize("authentication.name == 'admin')")

// Complex expression
@PreAuthorize("hasRole('TEACHER') and #courseId == principal.id")
```

#### 2. Programmatic Authorization

**UserService.requireRole():**
```java
public User requireRole(Role role) {
    User user = getCurrentUser();
    
    if (user.getRole() != role) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    
    return user;
}
```

**Usage in service:**
```java
@Transactional
public Course createCourse(CourseRequest request) {
    // Enforce that only teachers can create courses
    User teacher = userService.requireRole(Role.TEACHER);
    
    Course course = new Course();
    course.setTeacher(teacher);
    // ...
}
```

#### 3. URL-Based Authorization

**SecurityConfig:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/teacher/**").hasRole("TEACHER")
    .requestMatchers("/api/student/**").hasRole("STUDENT")
    .anyRequest().authenticated()
)
```

### Authorization Decision Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Request: POST /api/courses                                  │
│ Authentication: john_doe (ROLE_STUDENT)                     │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 1. Security Filter Chain                                    │
│    - JwtAuthenticationFilter validated token                │
│    - SecurityContext populated                              │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. URL-Based Check (.authorizeHttpRequests)                │
│    - /api/courses matches .anyRequest().authenticated()     │
│    - User is authenticated ✓                                │
│    - Continue to controller                                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Method-Level Check (@PreAuthorize)                      │
│    - Method has @PreAuthorize("hasRole('TEACHER')")        │
│    - User has ROLE_STUDENT                                  │
│    - Required: ROLE_TEACHER                                 │
│    - Match? NO ✗                                            │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Access Denied                                            │
│    - Throw AccessDeniedException                            │
│    - Return HTTP 403 Forbidden                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Attack Prevention

Your application prevents common security attacks:

### 1. SQL Injection

**Attack:**
```java
// VULNERABLE CODE (don't do this!)
String query = "SELECT * FROM users WHERE username = '" + username + "'";

// If username = "admin' OR '1'='1"
// Query becomes: SELECT * FROM users WHERE username = 'admin' OR '1'='1'
// Returns all users!
```

**Prevention:**
You use JPA/Hibernate with parameterized queries:
```java
userRepository.findByUsername(username);

// Hibernate generates:
// SELECT * FROM users WHERE username = ?
// Parameter is properly escaped
```

### 2. XSS (Cross-Site Scripting)

**Attack:**
```javascript
// Attacker submits username: <script>alert('hacked')</script>
// If displayed without escaping, JavaScript executes!
```

**Prevention:**
- Spring Boot escapes HTML by default
- Frontend frameworks (if you add React/Angular) also escape
- Content-Security-Policy headers (can be added)

### 3. Password Attacks

**Brute Force:**
- Attacker tries many passwords
- Prevention: Rate limiting (can be added), account lockout

**Rainbow Tables:**
- Pre-computed hashes for common passwords
- Prevention: BCrypt with salt (you're using this!)

**Password Sniffing:**
- Attacker intercepts network traffic
- Prevention: HTTPS (encrypt all traffic)

### 4. JWT Token Theft

**Scenario:** Attacker steals JWT token

**Mitigations:**
1. **Short expiration**: Your tokens expire in 1 hour
2. **HTTPS only**: Encrypt traffic (use HTTPS in production)
3. **HttpOnly cookies**: Alternative to localStorage (more secure)
4. **Refresh tokens**: Implement token refresh mechanism

### 5. CSRF (Prevented by JWT)

Your application uses JWT in Authorization header, not cookies.

**Why this prevents CSRF:**
- Browsers don't automatically send Authorization headers
- Attacker's malicious site can't include your token

---

## Step-by-Step Example: User Login

Let's trace a complete login from start to finish:

### Step 1: User Opens Login Page

**Browser requests:**
```
GET http://localhost:8081/index.html
```

**Server responds:**
```html
<!DOCTYPE html>
<html>
<body>
    <form id="login-form">
        <input name="username" placeholder="Username">
        <input name="password" type="password" placeholder="Password">
        <button type="submit">Login</button>
    </form>
    <script src="app.js"></script>
</body>
</html>
```

### Step 2: User Enters Credentials

```
Username: john_doe
Password: secret123
```

### Step 3: JavaScript Submits Form

**app.js:**
```javascript
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = e.target.username.value;
    const password = e.target.password.value;
    
    try {
        const response = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            throw new Error('Login failed');
        }
        
        const data = await response.json();
        localStorage.setItem('token', data.token);
        window.location.href = '/student.html';
    } catch (error) {
        alert('Login failed: ' + error.message);
    }
});
```

### Step 4: HTTP Request Sent

```
POST /api/auth/login HTTP/1.1
Host: localhost:8081
Content-Type: application/json
Content-Length: 47

{"username":"john_doe","password":"secret123"}
```

### Step 5: Spring Security Filter Chain

**No JWT filter check needed** (this is the login endpoint, public access).

### Step 6: Request Reaches Controller

**AuthController:**
```java
@PostMapping("/login")
public AuthResponse login(@Valid @RequestBody AuthRequest request) {
    return authService.login(request);
}
```

**Spring binds JSON:**
```java
AuthRequest request = new AuthRequest("john_doe", "secret123");
```

### Step 7: AuthService Authenticates

**AuthService.login():**
```java
public AuthResponse login(AuthRequest request) {
    // Authenticate user
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()
        )
    );
    
    // ... rest of method
}
```

### Step 8: Authentication Manager

**Internally:**

1. **Load user from database**
   ```sql
   SELECT * FROM users WHERE username = 'john_doe'
   ```
   
   Result:
   ```
   id = 1
   username = "john_doe"
   password_hash = "$2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ..."
   role = "STUDENT"
   ```

2. **Create UserDetails**
   ```java
   UserDetails userDetails = new User(
       "john_doe",
       "$2a$10$N9qo8uLOickgx2ZqFq9jG...",
       [ROLE_STUDENT]
   );
   ```

3. **Verify password**
   ```java
   boolean matches = passwordEncoder.matches(
       "secret123",                              // Submitted
       "$2a$10$N9qo8uLOickgx2ZqFq9jG..."        // Stored hash
   );
   // Returns: true ✓
   ```

4. **Return Authentication**
   ```java
   Authentication auth = new UsernamePasswordAuthenticationToken(
       userDetails,
       null,
       [ROLE_STUDENT]
   );
   return auth;
   ```

### Step 9: Generate JWT Token

**JwtService.generateToken():**
```java
String token = jwtService.generateToken(
    "john_doe",
    Map.of("role", "STUDENT")
);
```

**Generated token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNzA3MDQwODAwLCJleHAiOjE3MDcwNDQ0MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Decoded:**
```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "john_doe",
  "role": "STUDENT",
  "iat": 1707040800,    // 2026-02-04 10:00:00
  "exp": 1707044400     // 2026-02-04 11:00:00 (1 hour later)
}

// Signature (verified with secret key)
```

### Step 10: Return Response

**HTTP Response:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

### Step 11: JavaScript Stores Token

```javascript
localStorage.setItem('token', data.token);
```

**Browser's localStorage:**
```
key: "token"
value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Step 12: Redirect to Dashboard

```javascript
window.location.href = '/student.html';
```

**User is now logged in!**

---

## Step-by-Step Example: Accessing Protected Resource

Now john_doe (STUDENT) tries to enroll in a course.

### Step 1: User Clicks "Enroll" Button

**JavaScript:**
```javascript
async function enrollInCourse(courseId) {
    const token = localStorage.getItem('token');
    
    const response = await fetch('http://localhost:8081/api/registrations', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ courseId })
    });
    
    return response.json();
}

// User clicks enroll on course ID 5
enrollInCourse(5);
```

### Step 2: HTTP Request

```
POST /api/registrations HTTP/1.1
Host: localhost:8081
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{"courseId":5}
```

### Step 3: JwtAuthenticationFilter

**Filter extracts and validates token:**

```java
// 1. Extract header
String header = request.getHeader("Authorization");
// "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// 2. Extract token
String token = header.substring(7);
// "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// 3. Parse token
Claims claims = jwtService.parseClaims(token);
// Internally:
//   - Verify signature with secret key ✓
//   - Check expiration (exp: 1707044400, now: 1707040900) ✓
//   - Extract claims

// 4. Get username
String username = claims.getSubject();
// "john_doe"

// 5. Load user
UserDetails userDetails = userDetailsService.loadUserByUsername("john_doe");
// SQL: SELECT * FROM users WHERE username = 'john_doe'
// Returns: UserDetails with [ROLE_STUDENT]

// 6. Create Authentication
Authentication auth = new UsernamePasswordAuthenticationToken(
    userDetails,
    null,
    [ROLE_STUDENT]
);

// 7. Store in SecurityContext
SecurityContextHolder.getContext().setAuthentication(auth);
```

### Step 4: Request Reaches Controller

**RegistrationController:**
```java
@PostMapping
public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
    Registration registration = registrationService.register(request.courseId());
    return toResponse(registration);
}
```

**No @PreAuthorize** on this method, but requires authentication (from SecurityConfig).

### Step 5: Service Processes Enrollment

**RegistrationService.register():**
```java
@Transactional
public Registration register(Long courseId) {
    // 1. Get current user
    User student = userService.getCurrentUser();
    // Gets from SecurityContextHolder → john_doe (STUDENT)
    
    // 2. Verify is student
    userService.requireRole(Role.STUDENT);
    // john_doe has ROLE_STUDENT ✓
    
    // 3. Find course
    Course course = courseService.getCourseOrThrow(courseId);
    // SQL: SELECT * FROM courses WHERE id = 5
    
    // 4. Check if already enrolled
    boolean exists = registrationRepository.existsByStudentAndCourse(student, course);
    // SQL: SELECT COUNT(*) > 0 FROM registrations 
    //      WHERE student_id = 1 AND course_id = 5
    // Result: false
    
    if (exists) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
    }
    
    // 5. Create registration
    Registration registration = new Registration();
    registration.setStudent(student);
    registration.setCourse(course);
    
    // 6. Save
    return registrationRepository.save(registration);
    // SQL: INSERT INTO registrations (student_id, course_id) VALUES (1, 5)
}
```

### Step 6: Return Success Response

```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 42,
  "courseId": 5,
  "courseNo": "CS101",
  "courseName": "Introduction to Programming",
  "studentId": 1,
  "studentUsername": "john_doe"
}
```

### Step 7: JavaScript Updates UI

```javascript
alert('Successfully enrolled in CS101!');
loadMyCourses(); // Refresh course list
```

---

## Common Security Pitfalls

### 1. Storing Passwords in Plain Text

**NEVER do this:**
```java
user.setPassword(request.password());  // DANGER!
```

**Always hash:**
```java
user.setPasswordHash(passwordEncoder.encode(request.password()));
```

### 2. Exposing Sensitive Data in DTOs

**Bad:**
```java
public record UserResponse(
    Long id,
    String username,
    String passwordHash  // DANGER! Never expose password hash
) {}
```

**Good:**
```java
public record UserSummary(
    Long id,
    String username,
    String role
) {}
```

### 3. Not Validating Token Expiration

Your JwtService does this correctly:
```java
Claims claims = Jwts.parserBuilder()
    .setSigningKey(signingKey)
    .build()
    .parseClaimsJws(token)  // Throws exception if expired
    .getBody();
```

### 4. Weak JWT Secret

**Bad:**
```properties
app.jwt.secret=secret
```

**Good:**
```properties
app.jwt.secret=change-this-secret-to-a-long-random-string-at-least-32-chars
```

**Better (production):**
```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

### 5. Not Using HTTPS

In production, **always use HTTPS**:
- Encrypts all traffic
- Prevents token theft
- Prevents man-in-the-middle attacks

### 6. Storing JWT in localStorage (XSS vulnerable)

**Current approach:**
```javascript
localStorage.setItem('token', token);
```

**More secure (alternative):**
```javascript
// Use HttpOnly cookie (JavaScript can't access it)
// Set by server:
response.addCookie(new Cookie("token", token) {{
    setHttpOnly(true);
    setSecure(true);  // HTTPS only
    setPath("/");
}});
```

### 7. Not Implementing Token Refresh

**Current:** Token expires in 1 hour, user must login again

**Better:** Implement refresh tokens
- Access token: short-lived (15 min)
- Refresh token: long-lived (7 days)
- Use refresh token to get new access token

---

## Summary

### Authentication Flow
1. User submits credentials
2. Server verifies password (BCrypt comparison)
3. Server generates JWT token
4. Client stores token
5. Client includes token in requests
6. Server validates token signature and expiration
7. Server extracts username from token
8. Server populates SecurityContext

### Authorization Flow
1. Request authenticated (JWT validated)
2. SecurityContext contains user's roles
3. @PreAuthorize checks if user has required role
4. If yes: execute method
5. If no: return 403 Forbidden

### Key Security Principles
- **Never store plain-text passwords**: Use BCrypt
- **Stateless authentication**: JWT tokens
- **Role-based authorization**: STUDENT vs TEACHER
- **Defense in depth**: Multiple layers of security
- **Fail securely**: Default deny, explicit allow

### Your Application's Security Features
✓ Password hashing with BCrypt
✓ JWT-based authentication
✓ Role-based authorization
✓ Stateless sessions
✓ CSRF protection (JWT in header)
✓ SQL injection prevention (JPA)
✓ Method-level security (@PreAuthorize)
✓ Filter-based authentication

**You now understand how Spring Security orchestrates authentication and authorization in a modern web application!**
