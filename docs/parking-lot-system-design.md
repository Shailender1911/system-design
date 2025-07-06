# Parking Lot Management System - Industry Level Design

## Table of Contents
1. [System Overview](#system-overview)
2. [High Level Design (HLD)](#high-level-design-hld)
3. [Low Level Design (LLD)](#low-level-design-lld)
4. [SOLID Principles Implementation](#solid-principles-implementation)
5. [Design Patterns Used](#design-patterns-used)
6. [Database Design](#database-design)
7. [API Design](#api-design)
8. [Interview Questions](#interview-questions)
9. [Getting Started](#getting-started)

## System Overview

### Business Requirements
- **Multi-tenant parking lot management** with different pricing strategies
- **Real-time spot availability** tracking with distributed caching
- **Multiple vehicle types** support (Motorcycle, Car, Truck, Electric)
- **Dynamic pricing** based on demand, time, and location
- **Payment integration** with multiple payment gateways
- **Audit trail** for all operations
- **Notification system** for booking confirmations and reminders
- **Analytics and reporting** for business intelligence

### Key Features
- **Microservices architecture** with clear service boundaries
- **Event-driven architecture** for real-time updates
- **CQRS pattern** for read/write separation
- **Circuit breaker pattern** for resilience
- **Distributed caching** for performance
- **Security** with JWT authentication and role-based access control

## High Level Design (HLD)

### System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                     Load Balancer                                       │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │   API Gateway   │  │   API Gateway   │  │   API Gateway   │  │   API Gateway   │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │ Parking Lot     │  │ Vehicle         │  │ Payment         │  │ Notification    │     │
│  │ Management      │  │ Management      │  │ Service         │  │ Service         │     │
│  │ Service         │  │ Service         │  │                 │  │                 │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │ Pricing         │  │ Analytics       │  │ Audit           │  │ User            │     │
│  │ Service         │  │ Service         │  │ Service         │  │ Management      │     │
│  │                 │  │                 │  │                 │  │ Service         │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │ Message Queue   │  │ Redis Cache     │  │ Database        │  │ External        │     │
│  │ (RabbitMQ)      │  │ (Distributed)   │  │ (PostgreSQL)    │  │ Services        │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Service Boundaries

#### 1. **Parking Lot Management Service**
- **Responsibilities**: CRUD operations for parking lots, floors, and spots
- **Database**: PostgreSQL with read replicas
- **Cache**: Redis for frequently accessed data
- **Events**: Publishes parking lot events (created, updated, deactivated)

#### 2. **Vehicle Management Service**
- **Responsibilities**: Vehicle parking, exit, spot allocation
- **Database**: PostgreSQL with optimistic locking
- **Cache**: Redis for real-time availability
- **Events**: Publishes vehicle events (parked, exited)

#### 3. **Payment Service**
- **Responsibilities**: Payment processing, fee calculation, refunds
- **Database**: PostgreSQL with ACID compliance
- **External**: Payment gateway integration (Stripe, PayPal)
- **Events**: Publishes payment events

#### 4. **Pricing Service**
- **Responsibilities**: Dynamic pricing based on demand, time, location
- **Database**: PostgreSQL for pricing rules
- **Cache**: Redis for real-time prices
- **Algorithm**: ML-based demand prediction

#### 5. **Notification Service**
- **Responsibilities**: Email, SMS, push notifications
- **Database**: PostgreSQL for notification history
- **External**: Email service (SendGrid), SMS service (Twilio)
- **Queue**: Asynchronous message processing

#### 6. **Analytics Service**
- **Responsibilities**: Business intelligence, reporting, dashboards
- **Database**: Data warehouse (PostgreSQL/BigQuery)
- **Processing**: Real-time analytics with Apache Kafka

### Technology Stack

#### Backend
- **Language**: Java 17 with Spring Boot 3.x
- **Framework**: Spring Cloud for microservices
- **Database**: PostgreSQL 15 with connection pooling
- **Cache**: Redis 7.x with clustering
- **Message Queue**: RabbitMQ with dead letter queues
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka Server
- **Configuration**: Spring Cloud Config

#### Frontend
- **Framework**: React 18 with TypeScript
- **State Management**: Redux Toolkit
- **UI Library**: Material-UI
- **Build Tool**: Vite

#### DevOps
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Kubernetes with Helm charts
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Jaeger

### Data Flow

#### Vehicle Parking Flow
```
User Request → API Gateway → Vehicle Service → Spot Allocation → Payment Service → Notification Service
     ↓                                              ↓                      ↓
 JWT Validation                            Update Cache/DB         Send Confirmation
     ↓                                              ↓                      ↓
 Rate Limiting                           Publish Event            Update Analytics
```

#### Payment Processing Flow
```
Payment Request → Payment Service → External Gateway → Database Update → Event Publishing
       ↓                    ↓                ↓                  ↓              ↓
 Validation           Fraud Check      Process Payment    Update Ticket   Notify User
       ↓                    ↓                ↓                  ↓              ↓
 Authorization         Risk Assessment   Handle Response   Release Spot   Send Receipt
```

## Low Level Design (LLD)

### Core Domain Models

#### 1. **Parking Lot Aggregate**
```java
@Entity
@Table(name = "parking_lots")
public class ParkingLot extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Embedded
    private Address address;
    
    @Embedded
    private ParkingCapacity capacity;
    
    @Enumerated(EnumType.STRING)
    private ParkingLotStatus status;
    
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    private List<ParkingFloor> floors;
    
    // Domain methods
    public boolean hasAvailableSpots() { ... }
    public void reserveSpot() { ... }
    public void releaseSpot() { ... }
}
```

#### 2. **Vehicle Aggregate**
```java
@Entity
@Table(name = "vehicles")
public class Vehicle extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String licensePlate;
    
    @Enumerated(EnumType.STRING)
    private VehicleType type;
    
    @Embedded
    private VehicleOwner owner;
    
    @OneToMany(mappedBy = "vehicle")
    private List<ParkingSession> sessions;
    
    // Domain methods
    public boolean canParkIn(SpotType spotType) { ... }
    public ParkingSession startSession(ParkingSpot spot) { ... }
}
```

#### 3. **Parking Session Aggregate**
```java
@Entity
@Table(name = "parking_sessions")
public class ParkingSession extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String sessionId;
    
    @ManyToOne
    private Vehicle vehicle;
    
    @ManyToOne
    private ParkingSpot spot;
    
    @Embedded
    private SessionPeriod period;
    
    @Embedded
    private PaymentDetails payment;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    // Domain methods
    public Money calculateFee(PricingStrategy strategy) { ... }
    public void complete() { ... }
    public boolean isActive() { ... }
}
```

## SOLID Principles Implementation

### 1. **Single Responsibility Principle (SRP)**

Each service has a single, well-defined responsibility:

```java
// ✅ GOOD - Single responsibility
@Service
public class ParkingLotManagementService {
    public ParkingLot createParkingLot(CreateParkingLotCommand command) { ... }
    public void updateParkingLot(UpdateParkingLotCommand command) { ... }
    public void deactivateParkingLot(DeactivateParkingLotCommand command) { ... }
}

@Service
public class VehicleParkingService {
    public ParkingSession parkVehicle(ParkVehicleCommand command) { ... }
    public void exitVehicle(ExitVehicleCommand command) { ... }
}

@Service
public class PaymentService {
    public Payment processPayment(ProcessPaymentCommand command) { ... }
    public void refundPayment(RefundPaymentCommand command) { ... }
}
```

### 2. **Open/Closed Principle (OCP)**

Using Strategy pattern for extensible pricing:

```java
// ✅ GOOD - Open for extension, closed for modification
public interface PricingStrategy {
    Money calculatePrice(ParkingSession session);
}

@Component
public class HourlyPricingStrategy implements PricingStrategy {
    @Override
    public Money calculatePrice(ParkingSession session) {
        // Hourly pricing logic
    }
}

@Component
public class DemandBasedPricingStrategy implements PricingStrategy {
    @Override
    public Money calculatePrice(ParkingSession session) {
        // Demand-based pricing logic
    }
}
```

### 3. **Liskov Substitution Principle (LSP)**

Proper inheritance hierarchy:

```java
// ✅ GOOD - LSP compliant
public abstract class Vehicle {
    public abstract boolean canFitIn(ParkingSpot spot);
}

public class Car extends Vehicle {
    @Override
    public boolean canFitIn(ParkingSpot spot) {
        return spot.canAccommodate(VehicleType.CAR);
    }
}

public class Motorcycle extends Vehicle {
    @Override
    public boolean canFitIn(ParkingSpot spot) {
        return spot.canAccommodate(VehicleType.MOTORCYCLE);
    }
}
```

### 4. **Interface Segregation Principle (ISP)**

Specific interfaces for different concerns:

```java
// ✅ GOOD - Segregated interfaces
public interface Readable<T> {
    T findById(Long id);
    List<T> findAll();
}

public interface Writable<T> {
    T save(T entity);
    void delete(Long id);
}

public interface Searchable<T> {
    List<T> search(SearchCriteria criteria);
}

// Implementation can choose which interfaces to implement
public class ParkingLotRepository implements Readable<ParkingLot>, Writable<ParkingLot> {
    // Implementation
}
```

### 5. **Dependency Inversion Principle (DIP)**

Depend on abstractions, not concretions:

```java
// ✅ GOOD - Depends on abstractions
@Service
public class ParkingLotService {
    private final ParkingLotRepository repository;
    private final EventPublisher eventPublisher;
    private final PricingStrategy pricingStrategy;
    
    public ParkingLotService(
        ParkingLotRepository repository,
        EventPublisher eventPublisher,
        PricingStrategy pricingStrategy
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.pricingStrategy = pricingStrategy;
    }
}
```

## Design Patterns Used

### 1. **Strategy Pattern - Pricing Strategies**
```java
@Component
public class PricingContext {
    private final Map<PricingType, PricingStrategy> strategies;
    
    public Money calculatePrice(ParkingSession session, PricingType type) {
        return strategies.get(type).calculatePrice(session);
    }
}
```

### 2. **Factory Pattern - Entity Creation**
```java
@Component
public class ParkingSessionFactory {
    public ParkingSession createSession(Vehicle vehicle, ParkingSpot spot) {
        return ParkingSession.builder()
            .sessionId(generateSessionId())
            .vehicle(vehicle)
            .spot(spot)
            .period(SessionPeriod.startNow())
            .status(SessionStatus.ACTIVE)
            .build();
    }
}
```

### 3. **Command Pattern - Operations**
```java
public interface Command {
    void execute();
}

@Component
public class ParkVehicleCommand implements Command {
    private final VehicleParkingService service;
    private final ParkVehicleRequest request;
    
    @Override
    public void execute() {
        service.parkVehicle(request);
    }
}
```

### 4. **Observer Pattern - Event Handling**
```java
@EventListener
public class ParkingEventHandler {
    @Async
    public void handleVehicleParked(VehicleParkedEvent event) {
        // Send notification, update analytics, etc.
    }
}
```

### 5. **State Pattern - Session States**
```java
public abstract class SessionState {
    protected ParkingSession session;
    
    public abstract void park();
    public abstract void exit();
    public abstract void cancel();
}

public class ActiveState extends SessionState {
    @Override
    public void exit() {
        // Process exit logic
        session.setState(new CompletedState());
    }
}
```

## Database Design

### Why PostgreSQL?
- **ACID compliance** for financial transactions
- **JSON support** for flexible data structures
- **Excellent performance** for complex queries
- **Strong consistency** for critical operations
- **Extensibility** with custom functions and types

### Database Schema

```sql
-- Parking Lots
CREATE TABLE parking_lots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_zip_code VARCHAR(20),
    total_floors INTEGER NOT NULL,
    total_spots INTEGER NOT NULL,
    available_spots INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    pricing_config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Parking Spots
CREATE TABLE parking_spots (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    floor_number INTEGER NOT NULL,
    spot_number VARCHAR(20) NOT NULL,
    spot_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_parking_spots_lot_spot UNIQUE (parking_lot_id, spot_number)
);

-- Parking Sessions
CREATE TABLE parking_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    vehicle_license_plate VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    parking_spot_id BIGINT REFERENCES parking_spots(id),
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    amount_paid DECIMAL(10,2),
    payment_status VARCHAR(50) NOT NULL,
    session_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_parking_spots_lot_type_status ON parking_spots(parking_lot_id, spot_type, status);
CREATE INDEX idx_parking_sessions_license_status ON parking_sessions(vehicle_license_plate, session_status);
CREATE INDEX idx_parking_sessions_entry_time ON parking_sessions(entry_time);
```

## Interview Questions

### High Level Design (HLD) Questions

#### 1. **System Design Questions**

**Q: Design a parking lot management system that can handle 10,000 concurrent users across 100 parking lots.**

**Expected Answer:**
- **Load Balancing**: Use application load balancers to distribute traffic
- **Microservices Architecture**: Separate services for different domains
- **Database Sharding**: Partition data by parking lot ID or geographic region
- **Caching Strategy**: Redis for real-time availability, CDN for static content
- **Message Queues**: Asynchronous processing for non-critical operations
- **Auto-scaling**: Kubernetes HPA based on CPU/memory metrics

**Q: How would you handle real-time availability updates across multiple clients?**

**Expected Answer:**
- **WebSockets**: For real-time updates to mobile/web clients
- **Server-Sent Events**: For one-way updates
- **Event Sourcing**: Maintain event log for state reconstruction
- **CQRS**: Separate read/write models with eventual consistency
- **Message Broadcasting**: Use Redis pub/sub for real-time notifications

#### 2. **Scalability Questions**

**Q: How would you scale the system to handle Black Friday-like traffic spikes?**

**Expected Answer:**
- **Horizontal Scaling**: Auto-scaling groups with load balancers
- **Database Read Replicas**: Scale read operations
- **Caching Strategy**: Multi-layer caching (L1: Application, L2: Redis, L3: CDN)
- **Queue-based Processing**: Decouple heavy operations
- **Circuit Breakers**: Prevent cascade failures
- **Rate Limiting**: Protect against abuse

#### 3. **Data Consistency Questions**

**Q: How do you ensure data consistency when multiple users try to book the same parking spot?**

**Expected Answer:**
- **Optimistic Locking**: Version-based concurrency control
- **Pessimistic Locking**: Database row-level locking
- **Distributed Locking**: Redis-based distributed locks
- **Idempotency**: Ensure operations can be safely retried
- **Compensation Patterns**: Saga pattern for distributed transactions

### Low Level Design (LLD) Questions

#### 1. **Design Patterns Questions**

**Q: Which design patterns would you use and why?**

**Expected Answer:**
- **Strategy Pattern**: Different pricing strategies (hourly, daily, surge)
- **Factory Pattern**: Creating different types of vehicles/spots
- **Observer Pattern**: Event-driven notifications
- **Command Pattern**: Implementing undo/redo operations
- **State Pattern**: Managing parking session states
- **Repository Pattern**: Data access abstraction

#### 2. **Object-Oriented Design Questions**

**Q: Design the class hierarchy for different vehicle types.**

**Expected Answer:**
```java
public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType type;
    
    public abstract boolean canFitIn(ParkingSpot spot);
    public abstract double getParkingRate();
}

public class Car extends Vehicle {
    @Override
    public boolean canFitIn(ParkingSpot spot) {
        return spot.getType() == SpotType.COMPACT || 
               spot.getType() == SpotType.LARGE;
    }
}

public class Motorcycle extends Vehicle {
    @Override
    public boolean canFitIn(ParkingSpot spot) {
        return spot.getType() == SpotType.MOTORCYCLE ||
               spot.getType() == SpotType.COMPACT ||
               spot.getType() == SpotType.LARGE;
    }
}
```

#### 3. **SOLID Principles Questions**

**Q: How does your design follow SOLID principles?**

**Expected Answer:**
- **SRP**: Each class has single responsibility
- **OCP**: Open for extension through interfaces
- **LSP**: Subtypes are substitutable for base types
- **ISP**: Interfaces are focused and specific
- **DIP**: Depend on abstractions, not concretions

#### 4. **Database Design Questions**

**Q: How would you design the database schema for optimal performance?**

**Expected Answer:**
- **Normalization**: Reduce data redundancy
- **Indexing Strategy**: B-tree indexes for common queries
- **Partitioning**: Horizontal partitioning by date/location
- **Archiving**: Move old data to separate tables
- **Connection Pooling**: Optimize database connections

### Architecture Questions

#### 1. **Microservices vs Monolith**

**Q: When would you choose microservices over monolith?**

**Expected Answer:**
- **Team Size**: Multiple teams working independently
- **Scalability**: Different services have different scaling needs
- **Technology Diversity**: Different services can use different technologies
- **Deployment**: Independent deployment cycles
- **Fault Isolation**: Failure in one service doesn't bring down entire system

#### 2. **Event-Driven Architecture**

**Q: How would you implement event-driven architecture?**

**Expected Answer:**
- **Event Sourcing**: Store events as source of truth
- **Message Queues**: Async communication between services
- **Event Streaming**: Real-time event processing
- **Saga Pattern**: Distributed transaction management
- **Event Replay**: Ability to rebuild state from events

### Performance Questions

#### 1. **Caching Strategy**

**Q: How would you implement a multi-layer caching strategy?**

**Expected Answer:**
- **L1 Cache**: Application-level caching (Caffeine)
- **L2 Cache**: Distributed cache (Redis)
- **L3 Cache**: CDN for static content
- **Cache Invalidation**: Event-driven cache updates
- **Cache Warming**: Preload frequently accessed data

#### 2. **Database Optimization**

**Q: How would you optimize database queries for high load?**

**Expected Answer:**
- **Query Optimization**: Use EXPLAIN to analyze query plans
- **Index Strategy**: Composite indexes for multi-column queries
- **Connection Pooling**: Reuse database connections
- **Read Replicas**: Distribute read load
- **Query Caching**: Cache frequently executed queries

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose

### Running the Application

1. **Clone the repository**
```bash
git clone https://github.com/your-repo/parking-lot-system.git
cd parking-lot-system
```

2. **Start dependencies**
```bash
docker-compose up -d postgres redis rabbitmq
```

3. **Run the application**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **Access the application**
- API Documentation: http://localhost:8080/swagger-ui.html
- Application: http://localhost:8080/api/parking-lot
- Health Check: http://localhost:8080/actuator/health

### Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn test -Dtest=**/*IT

# Run performance tests
mvn test -Dtest=**/*PerformanceTest
```

### Monitoring

- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Jaeger**: http://localhost:16686 