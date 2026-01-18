# 🔐 Security Best Practices

Panduan keamanan untuk sistem microservices perpustakaan.

## 📑 Daftar Isi

- [Security Overview](#security-overview)
- [Authentication & Authorization](#authentication--authorization)
- [API Security](#api-security)
- [Data Security](#data-security)
- [Network Security](#network-security)
- [Container Security](#container-security)
- [Security Monitoring](#security-monitoring)

---

## Security Overview

### Security Layers

```
┌─────────────────────────────────────────┐
│         Network Security                │
│  - Firewall                             │
│  - TLS/SSL                              │
│  - VPN                                  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Application Security            │
│  - Authentication                       │
│  - Authorization                        │
│  - Input Validation                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Data Security                   │
│  - Encryption at rest                   │
│  - Encryption in transit                │
│  - Data masking                         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Infrastructure Security         │
│  - Container security                   │
│  - Secret management                    │
│  - Access control                       │
└─────────────────────────────────────────┘
```

---

## Authentication & Authorization

### Spring Security Configuration

**Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

**Security Configuration**:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // For stateless API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                
                // Authenticated endpoints
                .requestMatchers("/api/**").authenticated()
                
                // Role-based access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/anggota/**").hasAnyRole("ADMIN", "LIBRARIAN")
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        
        return jwtAuthConverter;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://app.example.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
```

### JWT Token Implementation

**Generate JWT**:
```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractClaims(token).getSubject();
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
```

### Method-Level Security

```java
@RestController
@RequestMapping("/api/anggota")
public class AnggotaController {
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<AnggotaCommand> create(@RequestBody CreateAnggotaCommand command) {
        // Only ADMIN or LIBRARIAN can create
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Only ADMIN can delete
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<AnggotaQuery> get(@PathVariable String id) {
        // Any authenticated user can view
    }
}
```

---

## API Security

### Input Validation

```java
@Data
public class CreateAnggotaCommand {
    
    @NotBlank(message = "Nomor anggota harus diisi")
    @Pattern(regexp = "^A\\d{3,}$", message = "Format nomor anggota tidak valid")
    @Size(max = 50, message = "Nomor anggota maksimal 50 karakter")
    private String nomorAnggota;
    
    @NotBlank(message = "Nama harus diisi")
    @Size(min = 3, max = 100, message = "Nama harus 3-100 karakter")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Nama hanya boleh huruf dan spasi")
    private String nama;
    
    @Email(message = "Format email tidak valid")
    @Size(max = 100, message = "Email maksimal 100 karakter")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Format telepon tidak valid")
    private String telepon;
}
```

### SQL Injection Prevention

```java
// ❌ Bad - Vulnerable to SQL injection
@Query(value = "SELECT * FROM anggota WHERE nama = '" + nama + "'", nativeQuery = true)
List<Anggota> findByNamaUnsafe(String nama);

// ✅ Good - Use parameterized queries
@Query("SELECT a FROM AnggotaCommand a WHERE a.nama = :nama")
List<AnggotaCommand> findByNama(@Param("nama") String nama);
```

### XSS Prevention

```java
import org.springframework.web.util.HtmlUtils;

@Service
public class AnggotaCommandService {
    
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        AnggotaCommand anggota = new AnggotaCommand();
        
        // Sanitize input
        anggota.setNama(HtmlUtils.htmlEscape(command.getNama()));
        anggota.setAlamat(HtmlUtils.htmlEscape(command.getAlamat()));
        
        return commandRepository.save(anggota);
    }
}
```

### Rate Limiting

**Using Bucket4j**:
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
}

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Bucket bucket;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded");
        }
    }
}
```

---

## Data Security

### Encryption at Rest

**Database Field Encryption**:
```java
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES";
    
    public String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}

@Entity
public class AnggotaCommand {
    
    @Column
    private String email;
    
    @PrePersist
    @PreUpdate
    public void encryptSensitiveData() {
        if (email != null) {
            email = encryptionService.encrypt(email);
        }
    }
    
    @PostLoad
    public void decryptSensitiveData() {
        if (email != null) {
            email = encryptionService.decrypt(email);
        }
    }
}
```

### Data Masking in Logs

```java
@Slf4j
@Service
public class AnggotaCommandService {
    
    public AnggotaCommand createAnggota(CreateAnggotaCommand command) {
        // Mask sensitive data in logs
        log.info("Creating anggota: nomor={}, nama={}, email={}", 
            command.getNomorAnggota(),
            command.getNama(),
            maskEmail(command.getEmail())
        );
        
        return commandRepository.save(anggota);
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return parts[0].substring(0, 1) + "***@" + parts[1];
    }
}
```

### Encryption in Transit (TLS/SSL)

**HTTPS Configuration**:
```properties
# Generate self-signed certificate (development only)
# keytool -genkeypair -alias perpustakaan -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650

server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=perpustakaan

# Redirect HTTP to HTTPS
server.http.port=8080
server.https.port=8443
```

---

## Network Security

### Firewall Rules

```bash
# Allow only necessary ports
sudo ufw allow 8080/tcp   # API Gateway
sudo ufw allow 8761/tcp   # Eureka (internal only)
sudo ufw deny 27017/tcp   # MongoDB (internal only)
sudo ufw deny 5672/tcp    # RabbitMQ (internal only)

# Enable firewall
sudo ufw enable
```

### Docker Network Isolation

```yaml
# docker-compose.yml
services:
  api-gateway:
    networks:
      - public
      - internal
  
  service-anggota:
    networks:
      - internal
  
  mongodb:
    networks:
      - internal

networks:
  public:
    driver: bridge
  internal:
    driver: bridge
    internal: true  # No external access
```

### Reverse Proxy (Nginx)

```nginx
# /etc/nginx/conf.d/perpustakaan.conf
upstream api_gateway {
    server api-gateway:8080;
}

server {
    listen 80;
    server_name api.perpustakaan.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.perpustakaan.com;
    
    # SSL certificates
    ssl_certificate /etc/letsencrypt/live/api.perpustakaan.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.perpustakaan.com/privkey.pem;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    
    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/m;
    limit_req zone=api_limit burst=20 nodelay;
    
    location / {
        proxy_pass http://api_gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## Container Security

### Docker Security Best Practices

**Dockerfile Security**:
```dockerfile
# Use specific version tags, not latest
FROM eclipse-temurin:17-jre-jammy

# Run as non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy only necessary files
COPY --chown=appuser:appuser target/*.jar app.jar

# Don't run as root
USER appuser

# Expose port
EXPOSE 8081

# Use exec form
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Scan for vulnerabilities**:
```bash
# Scan image with Trivy
trivy image service-anggota:latest

# Scan with Docker Scout
docker scout cves service-anggota:latest
```

### Secret Management

**Using Environment Variables**:
```yaml
# docker-compose.yml
services:
  service-anggota:
    environment:
      - MONGODB_PASSWORD_FILE=/run/secrets/mongodb_password
      - JWT_SECRET_FILE=/run/secrets/jwt_secret
    secrets:
      - mongodb_password
      - jwt_secret

secrets:
  mongodb_password:
    file: ./secrets/mongodb_password.txt
  jwt_secret:
    file: ./secrets/jwt_secret.txt
```

**Using HashiCorp Vault**:
```java
@Configuration
public class VaultConfig {
    
    @Value("${vault.uri}")
    private String vaultUri;
    
    @Value("${vault.token}")
    private String vaultToken;
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint vaultEndpoint = VaultEndpoint.from(URI.create(vaultUri));
        VaultEndpoint.VaultToken token = VaultToken.of(vaultToken);
        
        return new VaultTemplate(vaultEndpoint, new SimpleSessionManager(
            new ClientAuthentication() {
                @Override
                public VaultToken login() {
                    return token;
                }
            }
        ));
    }
}

@Service
public class SecretService {
    
    @Autowired
    private VaultTemplate vaultTemplate;
    
    public String getSecret(String path) {
        VaultResponseSupport<Map> response = vaultTemplate
            .read(path, Map.class);
        return (String) response.getData().get("value");
    }
}
```

---

## Security Monitoring

### Audit Logging

```java
@Aspect
@Component
@Slf4j
public class AuditAspect {
    
    @Autowired
    private AuditRepository auditRepository;
    
    @AfterReturning(
        pointcut = "@annotation(org.springframework.web.bind.annotation.PostMapping)",
        returning = "result"
    )
    public void auditCreate(JoinPoint joinPoint, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        AuditLog audit = new AuditLog();
        audit.setAction("CREATE");
        audit.setResource(joinPoint.getSignature().getDeclaringTypeName());
        audit.setUser(auth.getName());
        audit.setTimestamp(LocalDateTime.now());
        audit.setIpAddress(getClientIp());
        audit.setDetails(result.toString());
        
        auditRepository.save(audit);
    }
}
```

### Security Alerts

```java
@Service
public class SecurityMonitoringService {
    
    @Autowired
    private EmailService emailService;
    
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIp();
        
        log.warn("Failed login attempt: username={}, ip={}", username, ip);
        
        // Alert if too many failed attempts
        if (getFailedAttempts(username, ip) > 5) {
            emailService.sendAlert(
                "security@perpustakaan.com",
                "Multiple failed login attempts",
                String.format("User %s from IP %s has %d failed login attempts", 
                    username, ip, getFailedAttempts(username, ip))
            );
        }
    }
}
```

### Vulnerability Scanning

```bash
# OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check

# Snyk
snyk test

# SonarQube
mvn sonar:sonar \
  -Dsonar.projectKey=perpustakaan \
  -Dsonar.host.url=http://localhost:9000
```

---

## Security Checklist

### Development

- [ ] Input validation on all endpoints
- [ ] Parameterized queries (no SQL injection)
- [ ] XSS prevention
- [ ] CSRF protection
- [ ] Secure password hashing (BCrypt)
- [ ] No sensitive data in logs
- [ ] No hardcoded secrets

### Infrastructure

- [ ] HTTPS/TLS enabled
- [ ] Firewall configured
- [ ] Network segmentation
- [ ] Non-root containers
- [ ] Image vulnerability scanning
- [ ] Secret management (Vault/Docker Secrets)
- [ ] Regular security updates

### Application

- [ ] Authentication implemented
- [ ] Authorization checks
- [ ] Session management
- [ ] Rate limiting
- [ ] Security headers
- [ ] CORS configured
- [ ] API versioning

### Monitoring

- [ ] Audit logging
- [ ] Security alerts
- [ ] Failed login tracking
- [ ] Anomaly detection
- [ ] Regular security scans
- [ ] Penetration testing

---

[⬅️ Back to Documentation Index](README.md)