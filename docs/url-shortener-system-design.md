# URL Shortener System Design - Complete Implementation Guide

## Table of Contents
1. [System Overview](#system-overview)
2. [High-Level Design (HLD)](#high-level-design-hld)
3. [Low-Level Design (LLD)](#low-level-design-lld)
4. [Implementation Details](#implementation-details)
5. [Load Balancer vs API Gateway Question](#load-balancer-vs-api-gateway-question)
6. [Database Design](#database-design)
7. [Scalability & Performance](#scalability--performance)
8. [Security Considerations](#security-considerations)
9. [Interview Questions](#interview-questions)
10. [Monitoring & Observability](#monitoring--observability)

## System Overview

The URL shortener system is designed to convert long URLs into short, manageable links while providing analytics, expiration handling, and high availability. Key features include:

- **URL Shortening**: Convert long URLs to short, unique identifiers
- **Custom Aliases**: Allow users to create custom short URLs
- **Analytics**: Track clicks, geographic data, device information
- **Expiration**: Support for time-based URL expiration
- **Rate Limiting**: Prevent abuse and spam
- **High Availability**: 99.9% uptime with horizontal scaling

## High-Level Design (HLD)

### Architecture Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │────│   API Gateway   │────│  Rate Limiter   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  URL Shortener  │────│  URL Generation │────│   Analytics     │
│    Service      │    │    Service      │    │   Service       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Database      │────│   Cache Layer   │────│  Message Queue  │
│   (Primary)     │    │   (Redis)       │    │   (Kafka)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Data Flow

1. **URL Shortening Flow**:
   - Client sends POST request to `/api/v1/url/shorten`
   - Request passes through Load Balancer → API Gateway → Rate Limiter
   - URL Shortener Service generates short URL using counter or hash-based approach
   - Data stored in database and cached for quick access
   - Response returned with short URL

2. **URL Redirection Flow**:
   - Client clicks short URL (e.g., `https://short.ly/abc123`)
   - Request hits Load Balancer → API Gateway
   - Service checks cache first, then database
   - If found and valid, returns 301 redirect to original URL
   - Analytics event recorded asynchronously

## Low-Level Design (LLD)

### Service Layer Architecture

```java
@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {
    
    @Autowired
    private UrlGenerationService urlGenerationService;
    
    @Autowired
    private UrlAnalyticsService urlAnalyticsService;
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // Implementation methods...
}
```

### URL Generation Strategies

#### 1. Counter-Based Approach
```java
public String generateShortUrlWithCounter() {
    Long counter = redisTemplate.opsForValue().increment(COUNTER_KEY);
    return encodeToBase62(counter);
}
```

#### 2. Hash-Based Approach
```java
public String generateShortUrl(String originalUrl, int length) {
    String input = originalUrl + salt + System.currentTimeMillis();
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
    return encodeToBase62(hashBytes, length);
}
```

### Database Schema

#### URL Mappings Table
```sql
CREATE TABLE url_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_url VARCHAR(10) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    user_id VARCHAR(255),
    custom_alias VARCHAR(50),
    expiration_date TIMESTAMP,
    click_count BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_short_url (short_url),
    INDEX idx_user_id (user_id),
    INDEX idx_expiration_date (expiration_date),
    INDEX idx_created_at (created_at)
);
```

#### Analytics Table
```sql
CREATE TABLE url_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_url VARCHAR(10) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    country VARCHAR(2),
    city VARCHAR(100),
    device_type VARCHAR(50),
    browser VARCHAR(50),
    os VARCHAR(50),
    is_mobile BOOLEAN DEFAULT FALSE,
    is_bot BOOLEAN DEFAULT FALSE,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_short_url_analytics (short_url),
    INDEX idx_clicked_at (clicked_at),
    INDEX idx_country (country),
    INDEX idx_device_type (device_type)
);
```

## Implementation Details

### Key Implementation Features

#### 1. Caching Strategy
- **Redis** for high-performance caching
- **TTL-based expiration** for cache entries
- **Cache-aside pattern** for read operations
- **Write-through pattern** for critical updates

#### 2. Base62 Encoding
- Uses characters: `0-9`, `A-Z`, `a-z`
- 7-character length provides ~3.5 billion combinations
- Collision handling with retry mechanism

#### 3. Analytics Processing
- **Asynchronous processing** using message queues
- **User-agent parsing** for device/browser detection
- **GeoIP integration** for location tracking
- **Bot detection** for traffic filtering

#### 4. Rate Limiting
- **Token bucket algorithm** for user-based limiting
- **IP-based limiting** for anonymous users
- **Sliding window** for burst protection

## Load Balancer vs API Gateway Question

**Your Question**: "Why we have put load balancer before API services this might not work when there is DDoS attack, as there will be no rate limiting on Load Balancer its done on API gateway?"

**Answer**: This is an excellent architectural question! You're absolutely right to question this design. Here's the corrected approach:

### Recommended Architecture for DDoS Protection

```
Internet → DDoS Protection → WAF → API Gateway → Load Balancer → Services
```

#### Why This Order?

1. **DDoS Protection Layer**:
   - Services like AWS Shield, Cloudflare DDoS Protection
   - Filters malicious traffic before it reaches your infrastructure
   - Handles volumetric attacks (Layer 3/4)

2. **Web Application Firewall (WAF)**:
   - Filters application-layer attacks (Layer 7)
   - SQL injection, XSS protection
   - Geographic blocking

3. **API Gateway**:
   - **Rate limiting** (most important for your question)
   - Authentication/Authorization
   - Request validation
   - Circuit breaker patterns

4. **Load Balancer**:
   - Distributes legitimate traffic across instances
   - Health checks
   - SSL termination

#### Alternative: Load Balancer with Built-in Rate Limiting

Modern load balancers (like AWS ALB, NGINX Plus) support rate limiting:

```
Internet → DDoS Protection → Load Balancer (with rate limiting) → Services
```

#### Key Benefits of Correct Architecture:

1. **Early Traffic Filtering**: Malicious requests blocked before consuming resources
2. **Graduated Defense**: Multiple layers of protection
3. **Cost Efficiency**: Don't pay for processing malicious traffic
4. **Scalability**: Rate limiting at API Gateway scales with your application

### Updated Architecture Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ DDoS Protection │────│      WAF        │────│  API Gateway    │
│   (Cloudflare)  │    │  (Rate Limit)   │    │ (Auth/Validate) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Load Balancer   │────│ Service Layer   │────│   Cache Layer   │
│ (Distribution)  │    │ (Business Logic)│    │   (Redis)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Database Design

### Sharding Strategy

#### 1. Range-Based Sharding
```
Shard 1: short_url [0000000 - 1111111]
Shard 2: short_url [1111112 - 2222222]
Shard 3: short_url [2222223 - 3333333]
```

#### 2. Hash-Based Sharding
```java
public int getShardId(String shortUrl) {
    return Math.abs(shortUrl.hashCode()) % numberOfShards;
}
```

### Database Optimization

#### Indexes
- **Primary Index**: `short_url` (unique, clustered)
- **Secondary Indexes**: `user_id`, `created_at`, `expiration_date`
- **Composite Index**: `(user_id, created_at)` for user queries

#### Partitioning
- **Time-based partitioning** for analytics table
- **Monthly partitions** for better query performance
- **Automated partition management**

## Scalability & Performance

### Capacity Planning

#### Traffic Estimates
- **Write QPS**: 1,000/sec
- **Read QPS**: 100,000/sec (100:1 ratio)
- **Storage**: 1TB/year for URLs + 10TB/year for analytics

#### Infrastructure Sizing
```
Component          | Instances | CPU/Memory | Storage
-------------------|-----------|------------|----------
API Gateway        | 3         | 2vCPU/4GB  | -
URL Service        | 8         | 4vCPU/8GB  | -
Analytics Service  | 4         | 2vCPU/4GB  | -
Database (Primary) | 1         | 16vCPU/64GB| 2TB SSD
Database (Replica) | 2         | 8vCPU/32GB | 2TB SSD
Redis Cluster      | 3         | 4vCPU/16GB | 256GB SSD
```

### Performance Optimizations

#### 1. Connection Pooling
```java
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }
}
```

#### 2. Async Processing
```java
@Async
public CompletableFuture<Void> recordAnalytics(String shortUrl, HttpServletRequest request) {
    urlAnalyticsService.recordClick(shortUrl, request);
    return CompletableFuture.completedFuture(null);
}
```

## Security Considerations

### 1. Input Validation
```java
@Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
private String originalUrl;
```

### 2. Rate Limiting
```java
@RateLimiter(name = "url-shortener", fallbackMethod = "rateLimitFallback")
public ResponseEntity<ApiResponse<ShortenUrlResponse>> shortenUrl(ShortenUrlRequest request) {
    // Implementation
}
```

### 3. XSS Protection
```java
public String sanitizeInput(String input) {
    return StringEscapeUtils.escapeHtml4(input);
}
```

### 4. CSRF Protection
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }
}
```

## Interview Questions

### System Design Questions

#### 1. **Scalability Questions**

**Q: How would you handle 100 million URLs per day?**

**A**: 
- **Horizontal scaling**: Deploy multiple service instances
- **Database sharding**: Distribute data across multiple databases
- **Caching**: Redis cluster for frequently accessed URLs
- **CDN**: Cache static content and redirect responses
- **Load balancing**: Use consistent hashing for even distribution

**Q: What happens if your cache goes down?**

**A**:
- **Graceful degradation**: Fall back to database queries
- **Cache warming**: Preload frequently accessed URLs
- **Circuit breaker**: Prevent cascade failures
- **Monitoring**: Alert on cache availability
- **Redis Sentinel**: Automatic failover for Redis

#### 2. **Database Design Questions**

**Q: How would you design the database schema for analytics?**

**A**:
```sql
-- Time-partitioned analytics table
CREATE TABLE url_analytics_2024_01 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_url VARCHAR(10) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    country VARCHAR(2),
    city VARCHAR(100),
    device_type VARCHAR(50),
    browser VARCHAR(50),
    os VARCHAR(50),
    is_mobile BOOLEAN DEFAULT FALSE,
    is_bot BOOLEAN DEFAULT FALSE,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_short_url_time (short_url, clicked_at),
    INDEX idx_country_time (country, clicked_at)
) PARTITION BY RANGE (YEAR(clicked_at) * 100 + MONTH(clicked_at));
```

**Q: How would you handle URL collisions?**

**A**:
- **Unique constraints**: Database-level uniqueness
- **Retry mechanism**: Generate new URL if collision detected
- **Bloom filter**: Quick collision detection
- **Longer URLs**: Increase length if collision rate high
- **Collision tracking**: Monitor collision rates

#### 3. **Security Questions**

**Q: How would you prevent malicious URLs from being shortened?**

**A**:
- **URL validation**: Check against blacklists
- **Content scanning**: Analyze destination content
- **User reporting**: Allow users to report malicious URLs
- **Rate limiting**: Prevent spam creation
- **Captcha**: For suspicious patterns

**Q: How would you handle DDoS attacks?**

**A**:
- **DDoS protection service**: AWS Shield, Cloudflare
- **Rate limiting**: Per-IP and per-user limits
- **Geoblocking**: Block suspicious regions
- **Traffic analysis**: Identify attack patterns
- **Scaling**: Auto-scale to handle legitimate traffic

#### 4. **Performance Questions**

**Q: How would you optimize the redirection latency?**

**A**:
- **Edge caching**: CDN for popular URLs
- **Connection pooling**: Reuse database connections
- **Async analytics**: Don't block redirect for analytics
- **Local caching**: In-memory cache in application
- **Database optimization**: Proper indexing and query optimization

**Q: How would you handle hot URLs?**

**A**:
- **Multi-level caching**: Memory → Redis → Database
- **Read replicas**: Distribute read load
- **CDN**: Cache at edge locations
- **Load balancing**: Distribute traffic evenly
- **Monitoring**: Track hot URL patterns

#### 5. **Data Consistency Questions**

**Q: How would you ensure data consistency across multiple databases?**

**A**:
- **Database transactions**: ACID properties
- **Eventual consistency**: For analytics data
- **Compensation patterns**: Saga pattern for distributed transactions
- **Idempotency**: Ensure operations can be retried safely
- **Monitoring**: Track consistency violations

### Algorithm Questions

#### 1. **Base62 Encoding**
```java
public class Base62Encoder {
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    public String encode(long number) {
        if (number == 0) return "0";
        
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            result.append(BASE62_CHARS.charAt((int)(number % 62)));
            number /= 62;
        }
        return result.reverse().toString();
    }
    
    public long decode(String base62) {
        long result = 0;
        for (int i = 0; i < base62.length(); i++) {
            result = result * 62 + BASE62_CHARS.indexOf(base62.charAt(i));
        }
        return result;
    }
}
```

#### 2. **Consistent Hashing for Sharding**
```java
public class ConsistentHashing {
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodes;
    
    public ConsistentHashing(List<String> nodes, int virtualNodes) {
        this.virtualNodes = virtualNodes;
        for (String node : nodes) {
            addNode(node);
        }
    }
    
    public void addNode(String node) {
        for (int i = 0; i < virtualNodes; i++) {
            ring.put(hash(node + i), node);
        }
    }
    
    public String getNode(String key) {
        if (ring.isEmpty()) return null;
        
        Long hash = hash(key);
        if (!ring.containsKey(hash)) {
            SortedMap<Long, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }
        return ring.get(hash);
    }
    
    private Long hash(String key) {
        return (long) key.hashCode();
    }
}
```

### Behavioral Questions

#### 1. **Problem-Solving Approach**
**Q: Walk me through how you would debug a performance issue in the URL shortener.**

**A**:
1. **Identify symptoms**: High latency, error rates, timeouts
2. **Gather metrics**: APM tools, logs, database performance
3. **Isolate components**: API, database, cache, network
4. **Reproduce issue**: Load testing, environment comparison
5. **Root cause analysis**: Slow queries, memory leaks, configuration
6. **Fix and verify**: Deploy fix, monitor metrics, validate improvement

#### 2. **Trade-off Analysis**
**Q: SQL vs NoSQL for URL shortener?**

**A**:

**SQL (PostgreSQL/MySQL)**:
- **Pros**: ACID compliance, complex queries, mature ecosystem
- **Cons**: Vertical scaling limits, complex sharding
- **Use case**: Strong consistency requirements, complex analytics

**NoSQL (Cassandra/DynamoDB)**:
- **Pros**: Horizontal scaling, high availability, simple queries
- **Cons**: Eventual consistency, limited query flexibility
- **Use case**: High scale, simple key-value operations

**Recommendation**: Start with SQL for simplicity, migrate to NoSQL at scale.

### Code Quality Questions

#### 1. **Error Handling**
```java
@Service
public class UrlShortenerService {
    
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        try {
            validateRequest(request);
            String shortUrl = generateShortUrl(request);
            UrlMapping mapping = createMapping(shortUrl, request);
            return mapToResponse(mapping);
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw new BusinessException("Invalid request: " + e.getMessage());
        } catch (DuplicateKeyException e) {
            log.error("Duplicate short URL: {}", e.getMessage());
            throw new BusinessException("Short URL already exists");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new BusinessException("Internal server error");
        }
    }
}
```

#### 2. **Testing Strategy**
```java
@SpringBootTest
class UrlShortenerServiceTest {
    
    @Test
    void shouldShortenUrl() {
        // Given
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");
        
        // When
        ShortenUrlResponse response = urlShortenerService.shortenUrl(request);
        
        // Then
        assertThat(response.shortUrl()).isNotNull();
        assertThat(response.originalUrl()).isEqualTo("https://example.com");
    }
    
    @Test
    void shouldHandleDuplicateAlias() {
        // Given
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com", "duplicate");
        urlShortenerService.shortenUrl(request);
        
        // When & Then
        assertThatThrownBy(() -> urlShortenerService.shortenUrl(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");
    }
}
```

## Monitoring & Observability

### Key Metrics to Track

#### 1. **Business Metrics**
- URLs created per minute/hour/day
- Click-through rates
- Geographic distribution
- Popular domains/referrers
- User engagement metrics

#### 2. **Technical Metrics**
- API response times (p50, p95, p99)
- Error rates by endpoint
- Database query performance
- Cache hit/miss ratios
- Queue depth and processing times

#### 3. **Infrastructure Metrics**
- CPU/Memory utilization
- Database connections
- Network I/O
- Storage usage
- Auto-scaling events

### Monitoring Implementation

#### 1. **Application Metrics**
```java
@Component
public class UrlShortenerMetrics {
    private final Counter urlsCreated;
    private final Timer redirectionTime;
    private final Gauge cacheHitRate;
    
    public UrlShortenerMetrics(MeterRegistry meterRegistry) {
        this.urlsCreated = Counter.builder("urls.created")
            .description("Number of URLs created")
            .register(meterRegistry);
        
        this.redirectionTime = Timer.builder("redirection.time")
            .description("Time taken for URL redirection")
            .register(meterRegistry);
        
        this.cacheHitRate = Gauge.builder("cache.hit.rate")
            .description("Cache hit rate percentage")
            .register(meterRegistry, this, UrlShortenerMetrics::getCacheHitRate);
    }
    
    public void incrementUrlsCreated() {
        urlsCreated.increment();
    }
    
    public void recordRedirectionTime(Duration duration) {
        redirectionTime.record(duration);
    }
    
    private double getCacheHitRate() {
        // Calculate cache hit rate logic
        return 0.95; // 95% hit rate
    }
}
```

#### 2. **Health Checks**
```java
@Component
public class UrlShortenerHealthIndicator implements HealthIndicator {
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            urlMappingRepository.count();
            
            // Check Redis connectivity
            redisTemplate.opsForValue().get("health-check");
            
            return Health.up()
                .withDetail("database", "UP")
                .withDetail("cache", "UP")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Alerting Rules

#### 1. **Critical Alerts**
- API error rate > 1%
- Database connection failures
- Cache unavailable
- Response time > 500ms (p95)

#### 2. **Warning Alerts**
- URL creation rate spike (>2x normal)
- High cache miss rate (>20%)
- Database query time > 100ms
- Storage usage > 80%

---

This comprehensive guide covers all aspects of designing and implementing a URL shortener system. The implementation includes proper error handling, security measures, scalability considerations, and addresses your specific question about load balancer placement in the context of DDoS protection. 