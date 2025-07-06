# URL Shortener System - Complete Implementation

## Overview

This is a production-ready URL shortener system built with Spring Boot, featuring:

- **High Performance**: Handle millions of requests with Redis caching and optimized database queries
- **Scalable Architecture**: Horizontal scaling with load balancing and sharding support
- **Advanced Analytics**: Real-time click tracking with geographic and device insights
- **Security**: Rate limiting, input validation, and DDoS protection
- **Custom Aliases**: Support for user-defined short URLs
- **Expiration Management**: Time-based URL expiration with automatic cleanup

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ DDoS Protection │────│  API Gateway    │────│  Rate Limiter   │
│   (Cloudflare)  │    │ (Auth/Validate) │    │ (Token Bucket)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Load Balancer   │────│ Service Layer   │────│   Cache Layer   │
│ (Distribution)  │    │ (Business Logic)│    │   (Redis)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Primary Database│────│ Analytics DB    │────│  Message Queue  │
│   (MySQL)       │    │  (Time Series)  │    │   (Kafka)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Key Features Implemented

### 1. URL Shortening Service
- **Multiple Generation Strategies**: Hash-based and counter-based approaches
- **Base62 Encoding**: Compact, URL-safe short codes
- **Collision Handling**: Retry mechanism with exponential backoff
- **Custom Aliases**: User-defined short URLs with validation

### 2. High-Performance Caching
- **Redis Integration**: Multi-level caching strategy
- **Cache-Aside Pattern**: Optimal read performance
- **TTL Management**: Automatic cache expiration
- **Cache Warming**: Preload popular URLs

### 3. Advanced Analytics
- **Real-time Tracking**: Click events with device/browser detection
- **Geographic Analytics**: Country and city-level insights
- **Bot Detection**: Filter non-human traffic
- **Time-series Data**: Hourly/daily click patterns

### 4. Security & Rate Limiting
- **Token Bucket Algorithm**: User and IP-based rate limiting
- **Input Validation**: XSS and injection protection
- **HTTPS Enforcement**: SSL/TLS encryption
- **CORS Configuration**: Cross-origin request handling

## Technical Implementation

### Core Components

#### 1. Entity Classes
```java
@Entity
@Table(name = "url_mappings")
public class UrlMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String shortUrl;
    
    @Column(columnDefinition = "TEXT")
    private String originalUrl;
    
    private LocalDateTime expirationDate;
    private Long clickCount = 0L;
    private Boolean isActive = true;
}
```

#### 2. Service Layer
```java
@Service
@Transactional
public class UrlShortenerServiceImpl implements UrlShortenerService {
    
    @Autowired private UrlGenerationService urlGenerationService;
    @Autowired private UrlAnalyticsService urlAnalyticsService;
    @Autowired private UrlMappingRepository repository;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        // Generate short URL with collision handling
        // Store in database and cache
        // Return response with analytics tracking
    }
}
```

#### 3. Analytics Processing
```java
@Service
public class UrlAnalyticsServiceImpl implements UrlAnalyticsService {
    
    public void recordClick(String shortUrl, HttpServletRequest request) {
        // Extract IP, user agent, referrer
        // Parse device/browser information
        // Store analytics data asynchronously
    }
}
```

### Database Schema

#### URL Mappings Table
```sql
CREATE TABLE url_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_url VARCHAR(10) UNIQUE NOT NULL,
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
    INDEX idx_expiration_date (expiration_date)
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
    INDEX idx_country (country)
);
```

## API Endpoints

### 1. URL Management
```http
POST /api/v1/url/shorten
Content-Type: application/json

{
    "originalUrl": "https://example.com/very/long/url",
    "customAlias": "my-link",
    "expirationDate": "2024-12-31T23:59:59",
    "description": "My custom short link"
}
```

### 2. URL Redirection
```http
GET /{shortUrl}
# Returns 301 redirect to original URL
```

### 3. Analytics
```http
GET /api/v1/url/{shortUrl}/analytics
# Returns comprehensive analytics data
```

### 4. User Management
```http
GET /api/v1/url/user/{userId}?page=0&size=20
# Returns paginated user URLs
```

## Performance Characteristics

### Capacity Planning
- **Write Throughput**: 1,000 URLs/second
- **Read Throughput**: 100,000 redirects/second
- **Storage Efficiency**: 127 bytes per URL mapping
- **Cache Hit Rate**: 95%+ for popular URLs
- **Response Time**: <50ms for cached redirects

### Scalability Features
- **Horizontal Scaling**: Stateless service layer
- **Database Sharding**: Range and hash-based partitioning
- **Read Replicas**: Separate read and write workloads
- **CDN Integration**: Global edge caching

## Load Balancer vs API Gateway Architecture

### The Question Addressed
**"Why we have put load balancer before API services this might not work when there is DDoS attack, as there will be no rate limiting on Load Balancer its done on API gateway?"**

### Correct Architecture for DDoS Protection

```
Internet → DDoS Protection → WAF → API Gateway → Load Balancer → Services
```

#### Why This Order?

1. **DDoS Protection Layer**: Filters volumetric attacks before they reach infrastructure
2. **Web Application Firewall**: Protects against application-layer attacks
3. **API Gateway**: Implements rate limiting, authentication, and request validation
4. **Load Balancer**: Distributes legitimate traffic across service instances

#### Key Benefits:
- **Early Filtering**: Malicious requests blocked before consuming resources
- **Cost Efficiency**: Don't pay for processing malicious traffic
- **Graduated Defense**: Multiple protection layers
- **Scalability**: Rate limiting scales with application needs

## Setup Instructions

### Prerequisites
- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### Local Development Setup

1. **Clone Repository**
```bash
git clone <repository-url>
cd url-shortener-system
```

2. **Database Setup**
```sql
CREATE DATABASE url_shortener;
CREATE USER 'url_shortener_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON url_shortener.* TO 'url_shortener_user'@'localhost';
```

3. **Redis Setup**
```bash
# Start Redis server
redis-server

# Verify connection
redis-cli ping
```

4. **Configuration**
```bash
# Copy and modify application properties
cp src/main/resources/application-url-shortener.properties.example \
   src/main/resources/application-url-shortener.properties

# Update database and Redis connection details
```

5. **Build and Run**
```bash
mvn clean package
java -jar target/system-design-url-shortener.jar --spring.profiles.active=url-shortener
```

### Production Deployment

#### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/system-design-url-shortener.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: url-shortener
spec:
  replicas: 3
  selector:
    matchLabels:
      app: url-shortener
  template:
    metadata:
      labels:
        app: url-shortener
    spec:
      containers:
      - name: url-shortener
        image: url-shortener:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "url-shortener,production"
```

## Monitoring and Observability

### Key Metrics
- **Business Metrics**: URLs created, click-through rates, geographic distribution
- **Technical Metrics**: Response times, error rates, cache hit ratios
- **Infrastructure Metrics**: CPU/memory usage, database performance

### Health Checks
```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/prometheus
```

### Alerting Rules
- API error rate > 1%
- Response time > 500ms (p95)
- Cache miss rate > 20%
- Database connection failures

## Testing Strategy

### Unit Tests
```java
@SpringBootTest
class UrlShortenerServiceTest {
    
    @Test
    void shouldShortenUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com");
        ShortenUrlResponse response = service.shortenUrl(request);
        
        assertThat(response.shortUrl()).isNotNull();
        assertThat(response.originalUrl()).isEqualTo("https://example.com");
    }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerIntegrationTest {
    
    @Test
    void shouldRedirectToOriginalUrl() {
        // Create short URL
        // Test redirection
        // Verify analytics
    }
}
```

### Load Testing
```bash
# Using Apache Bench
ab -n 10000 -c 100 http://localhost:8080/api/v1/url/shorten

# Using JMeter
jmeter -n -t url-shortener-load-test.jmx
```

## Security Considerations

### Input Validation
- URL format validation with regex patterns
- XSS protection with input sanitization
- SQL injection prevention with parameterized queries

### Rate Limiting
- Token bucket algorithm implementation
- Per-user and per-IP limits
- Graceful degradation under load

### Authentication & Authorization
- JWT token-based authentication
- Role-based access control
- API key management for external clients

## Interview Questions Covered

### System Design Questions
1. How to handle 100 million URLs per day?
2. Database design for analytics data
3. Handling URL collisions
4. DDoS protection strategies
5. Caching strategies for hot URLs

### Algorithm Questions
1. Base62 encoding/decoding implementation
2. Consistent hashing for sharding
3. Rate limiting algorithms
4. Bloom filters for collision detection

### Scalability Questions
1. Horizontal vs vertical scaling trade-offs
2. Database sharding strategies
3. Cache invalidation patterns
4. Load balancing algorithms

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ for learning system design principles and implementing scalable web services.** 