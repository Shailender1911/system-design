# Zepto System Design - Ultra-Fast Grocery Delivery

## Table of Contents
1. [System Overview](#system-overview)
2. [High Level Design (HLD)](#high-level-design-hld)
3. [Low Level Design (LLD)](#low-level-design-lld)
4. [Database Design](#database-design)
5. [Microservices Architecture](#microservices-architecture)
6. [Interview Questions](#interview-questions)
7. [Implementation Guide](#implementation-guide)

## System Overview

### Business Requirements
- **Ultra-fast delivery** (10-15 minutes) for essential grocery items
- **Real-time inventory management** across multiple micro-warehouses
- **Dynamic pricing** based on demand, inventory, and location
- **Route optimization** for delivery partners
- **Multi-tenant architecture** supporting multiple cities
- **Real-time tracking** for customers and delivery partners
- **Payment integration** with multiple payment methods
- **Surge pricing** during peak hours
- **Recommendation engine** for personalized suggestions

### Key Features
- **Dark store network** with hyper-local inventory
- **Real-time order matching** with nearest available inventory
- **Intelligent routing** for delivery optimization
- **Demand forecasting** using machine learning
- **Dynamic inventory allocation** across stores
- **Multi-channel notifications** (SMS, Push, Email)
- **Loyalty program** and referral system
- **Analytics dashboard** for business insights

### Scale Requirements
- **10M+ daily active users**
- **100K+ orders per hour** during peak times
- **1000+ dark stores** across multiple cities
- **10K+ delivery partners**
- **99.9% uptime** requirement
- **Sub-second response times** for critical operations

## High Level Design (HLD)

### System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                            Load Balancer (Global)                                                │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │   API Gateway   │  │   API Gateway   │  │   API Gateway   │  │   API Gateway   │  │   API Gateway   │        │
│  │   (Mumbai)      │  │   (Delhi)       │  │   (Bangalore)   │  │   (Hyderabad)   │  │   (Chennai)     │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                           Core Services Layer                                                     │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ User Service    │  │ Catalog Service │  │ Inventory       │  │ Order Service   │  │ Cart Service    │        │
│  │                 │  │                 │  │ Service         │  │                 │  │                 │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ Payment Service │  │ Delivery Service│  │ Notification    │  │ Recommendation  │  │ Pricing Service │        │
│  │                 │  │                 │  │ Service         │  │ Service         │  │                 │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ Analytics       │  │ Loyalty Service │  │ Warehouse       │  │ Route           │  │ Demand          │        │
│  │ Service         │  │                 │  │ Service         │  │ Optimization    │  │ Forecasting     │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                         Data & Infrastructure Layer                                              │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ PostgreSQL      │  │ MongoDB         │  │ Redis           │  │ Elasticsearch   │  │ Apache Kafka    │        │
│  │ (Transactional) │  │ (Catalog)       │  │ (Cache/Session) │  │ (Search)        │  │ (Event Stream)  │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ ClickHouse      │  │ MinIO           │  │ Prometheus      │  │ Grafana         │  │ Jaeger          │        │
│  │ (Analytics)     │  │ (Object Store)  │  │ (Metrics)       │  │ (Dashboards)    │  │ (Tracing)       │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

### Service Boundaries & Responsibilities

#### 1. **User Service**
- **Responsibilities**: User registration, authentication, profile management
- **Database**: PostgreSQL for user data
- **Cache**: Redis for session management
- **Events**: User registration, profile updates

#### 2. **Catalog Service**
- **Responsibilities**: Product catalog, categories, pricing
- **Database**: MongoDB for flexible product schema
- **Cache**: Redis for frequently accessed products
- **Search**: Elasticsearch for product search

#### 3. **Inventory Service**
- **Responsibilities**: Real-time inventory tracking, stock allocation
- **Database**: PostgreSQL with row-level locking
- **Cache**: Redis for real-time availability
- **Events**: Stock updates, allocation events

#### 4. **Order Service**
- **Responsibilities**: Order creation, status management, order history
- **Database**: PostgreSQL for ACID compliance
- **Events**: Order placed, status changes, completion

#### 5. **Cart Service**
- **Responsibilities**: Shopping cart management, session handling
- **Database**: Redis for session-based data
- **Cache**: In-memory for ultra-fast operations

#### 6. **Payment Service**
- **Responsibilities**: Payment processing, refunds, wallet management
- **Database**: PostgreSQL for financial data
- **External**: Payment gateway integration
- **Events**: Payment success/failure events

#### 7. **Delivery Service**
- **Responsibilities**: Delivery partner management, order assignment
- **Database**: PostgreSQL with geospatial queries
- **Cache**: Redis for real-time partner locations
- **Events**: Delivery assignment, status updates

#### 8. **Notification Service**
- **Responsibilities**: SMS, push notifications, email alerts
- **Database**: PostgreSQL for notification history
- **External**: SMS gateway, push notification services
- **Queue**: Async message processing

#### 9. **Route Optimization Service**
- **Responsibilities**: Optimal route calculation, ETA prediction
- **Database**: PostgreSQL for route data
- **External**: Maps API for real-time traffic
- **ML**: Route optimization algorithms

#### 10. **Demand Forecasting Service**
- **Responsibilities**: Demand prediction, inventory planning
- **Database**: ClickHouse for time-series data
- **ML**: TensorFlow/PyTorch for forecasting models
- **Events**: Demand predictions, inventory alerts

### Technology Stack

#### Backend Services
- **Language**: Java 17 with Spring Boot 3.x
- **Framework**: Spring Cloud for microservices
- **API Gateway**: Spring Cloud Gateway with circuit breakers
- **Service Discovery**: Consul
- **Configuration**: Spring Cloud Config with Consul backend

#### Databases
- **OLTP**: PostgreSQL 15 with connection pooling
- **NoSQL**: MongoDB 6.x for product catalog
- **Cache**: Redis 7.x with clustering
- **Search**: Elasticsearch 8.x
- **Analytics**: ClickHouse for real-time analytics
- **Object Storage**: MinIO for images and documents

#### Message Queue & Event Streaming
- **Message Queue**: Apache Kafka for high-throughput events
- **Dead Letter Queue**: Kafka topics for failed messages
- **Event Sourcing**: Kafka for audit trail

#### Mobile & Web
- **Mobile**: React Native with TypeScript
- **Web**: React 18 with Next.js
- **State Management**: Redux Toolkit Query
- **Real-time**: WebSocket for live tracking

#### Machine Learning
- **Framework**: TensorFlow Serving
- **Model Store**: MLflow
- **Feature Store**: Feast
- **A/B Testing**: Optimizely

#### Infrastructure
- **Container**: Docker with multi-stage builds
- **Orchestration**: Kubernetes with Helm
- **Service Mesh**: Istio for traffic management
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack
- **Tracing**: Jaeger

### Data Flow

#### Order Processing Flow
```
User Places Order → Inventory Check → Payment Processing → Order Confirmation → Warehouse Assignment → Picker Assignment → Delivery Partner Assignment → Route Optimization → Delivery → Order Completion
```

#### Real-time Inventory Flow
```
Inventory Update → Event Published → Cache Update → Availability Check → Real-time Sync → Frontend Update
```

#### Delivery Assignment Flow
```
Order Ready → Find Available Partners → Calculate Routes → Assign Optimal Partner → Send Notification → Start Delivery
```

## Low Level Design (LLD)

### Core Domain Models

#### 1. **User Aggregate**
```java
@Entity
@Table(name = "users")
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(unique = true)
    private String email;
    
    @Embedded
    private UserProfile profile;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    // Domain methods
    public void addAddress(Address address) { ... }
    public Address getPrimaryAddress() { ... }
    public boolean isActive() { ... }
}
```

#### 2. **Product Aggregate**
```java
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String brand;
    private ProductCategory category;
    private List<String> tags;
    private List<ProductImage> images;
    private NutritionInfo nutrition;
    private ProductPricing pricing;
    private ProductInventory inventory;
    private ProductAttributes attributes;
    
    // Domain methods
    public boolean isAvailable() { ... }
    public Money getPrice(String city) { ... }
    public boolean canDeliver(String pincode) { ... }
}
```

#### 3. **Order Aggregate**
```java
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne
    private User customer;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @Embedded
    private OrderPricing pricing;
    
    @Embedded
    private DeliveryInfo delivery;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    // Domain methods
    public Money calculateTotal() { ... }
    public void updateStatus(OrderStatus status) { ... }
    public boolean canCancel() { ... }
}
```

#### 4. **Inventory Aggregate**
```java
@Entity
@Table(name = "inventory")
public class Inventory extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Product product;
    
    @ManyToOne
    private Warehouse warehouse;
    
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    
    @Embedded
    private InventoryThresholds thresholds;
    
    // Domain methods
    public boolean reserve(Integer quantity) { ... }
    public void release(Integer quantity) { ... }
    public boolean isLowStock() { ... }
}
```

### Design Patterns Implementation

#### 1. **Strategy Pattern - Pricing Strategies**
```java
public interface PricingStrategy {
    Money calculatePrice(Product product, PricingContext context);
}

@Component
public class DynamicPricingStrategy implements PricingStrategy {
    @Override
    public Money calculatePrice(Product product, PricingContext context) {
        // Implement demand-based pricing
        Money basePrice = product.getBasePrice();
        double demandMultiplier = context.getDemandMultiplier();
        double inventoryMultiplier = context.getInventoryMultiplier();
        
        return basePrice.multiply(demandMultiplier).multiply(inventoryMultiplier);
    }
}
```

#### 2. **Command Pattern - Order Operations**
```java
public interface OrderCommand {
    OrderResult execute();
}

@Component
public class PlaceOrderCommand implements OrderCommand {
    private final OrderService orderService;
    private final PlaceOrderRequest request;
    
    @Override
    public OrderResult execute() {
        // Validate inventory
        // Process payment
        // Create order
        // Send notifications
        return OrderResult.success(order);
    }
}
```

#### 3. **Observer Pattern - Event Handling**
```java
@EventListener
public class OrderEventHandler {
    
    @Async
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Update inventory
        // Send notification
        // Update analytics
    }
    
    @Async
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        // Update loyalty points
        // Send feedback request
        // Update delivery metrics
    }
}
```

#### 4. **Saga Pattern - Distributed Transactions**
```java
@Component
public class OrderSaga {
    
    @SagaOrchestrationStart
    public void processOrder(OrderPlacedEvent event) {
        // Step 1: Reserve inventory
        choreography.step("reserveInventory")
            .compensate("releaseInventory");
        
        // Step 2: Process payment
        choreography.step("processPayment")
            .compensate("refundPayment");
        
        // Step 3: Assign delivery
        choreography.step("assignDelivery")
            .compensate("cancelDelivery");
    }
}
```

## Database Design

### Database Selection Strategy

#### **PostgreSQL for Transactional Data**
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    warehouse_id BIGINT REFERENCES warehouses(id),
    delivery_address_id BIGINT REFERENCES addresses(id),
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    delivery_fee DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory table with optimistic locking
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    warehouse_id BIGINT REFERENCES warehouses(id),
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER DEFAULT 0,
    total_quantity INTEGER NOT NULL,
    min_threshold INTEGER DEFAULT 10,
    max_threshold INTEGER DEFAULT 1000,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory_product_warehouse UNIQUE (product_id, warehouse_id)
);

-- Indexes for performance
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_warehouse_created ON orders(warehouse_id, created_at);
CREATE INDEX idx_inventory_product_available ON inventory(product_id, available_quantity);
```

#### **MongoDB for Product Catalog**
```javascript
// Products collection
{
  _id: ObjectId("..."),
  sku: "PROD-001",
  name: "Organic Bananas",
  description: "Fresh organic bananas",
  brand: "Nature's Best",
  category: {
    id: "fruits",
    name: "Fruits",
    hierarchy: ["grocery", "fresh", "fruits"]
  },
  images: [
    {
      url: "https://cdn.zepto.com/products/prod-001-1.jpg",
      alt: "Organic Bananas Front View",
      order: 1
    }
  ],
  nutrition: {
    calories: 105,
    protein: 1.3,
    carbs: 27,
    fiber: 3.1,
    sugar: 14.4
  },
  pricing: {
    basePrice: 40.00,
    currency: "INR",
    unit: "per kg",
    cityPricing: {
      "mumbai": { price: 45.00, surge: 1.1 },
      "delhi": { price: 42.00, surge: 1.0 }
    }
  },
  attributes: {
    organic: true,
    weight: "1kg",
    shelfLife: "3-5 days",
    origin: "Maharashtra"
  },
  searchTerms: ["banana", "organic", "fruit", "healthy"],
  tags: ["organic", "seasonal", "popular"],
  isActive: true,
  createdAt: ISODate("2024-01-01T00:00:00Z"),
  updatedAt: ISODate("2024-01-01T00:00:00Z")
}
```

#### **Redis for Caching & Sessions**
```redis
# Product availability cache
SET product:availability:PROD-001:warehouse:WH-001 150 EX 300

# User session
HSET user:session:123 
  user_id 123 
  cart_id "cart:123" 
  location "mumbai" 
  last_activity 1641897600

# Real-time inventory
ZADD inventory:low_stock 
  5 "PROD-001:WH-001" 
  8 "PROD-002:WH-001" 
  12 "PROD-003:WH-001"

# Cart data
HSET cart:123 
  items '{"PROD-001":{"quantity":2,"price":45.00}}'
  total 90.00 
  expires_at 1641897600
```

## Microservices Architecture

### Service Communication Patterns

#### 1. **Synchronous Communication**
```java
// Order Service calling Inventory Service
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    
    @PostMapping("/api/inventory/reserve")
    InventoryReservationResponse reserveInventory(
        @RequestBody InventoryReservationRequest request);
    
    @PostMapping("/api/inventory/release")
    void releaseInventory(@RequestBody InventoryReleaseRequest request);
}

// With Circuit Breaker
@Component
public class InventoryServiceClient {
    
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "fallbackReserveInventory")
    public InventoryReservationResponse reserveInventory(InventoryReservationRequest request) {
        return inventoryClient.reserveInventory(request);
    }
    
    public InventoryReservationResponse fallbackReserveInventory(InventoryReservationRequest request, Exception ex) {
        // Fallback logic - maybe reserve from different warehouse
        return InventoryReservationResponse.unavailable();
    }
}
```

#### 2. **Asynchronous Communication**
```java
// Event Publishing
@Component
public class OrderEventPublisher {
    
    @EventListener
    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send("order-events", event);
    }
}

// Event Consumption
@KafkaListener(topics = "order-events")
public class InventoryEventConsumer {
    
    @KafkaHandler
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Update inventory allocation
        inventoryService.allocateInventory(event.getOrderId(), event.getItems());
    }
}
```

### API Gateway Configuration

```yaml
# Spring Cloud Gateway routes
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: user-service
                fallbackUri: forward:/fallback/user
            - name: RateLimiter
              args:
                rate-limiter: redis
                key-resolver: "#{@userKeyResolver}"
        
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - name: Authentication
            - name: CircuitBreaker
              args:
                name: order-service
```

## Interview Questions

### High Level Design Questions

#### 1. **Scale and Performance**

**Q: How would you design Zepto to handle 100,000 orders per minute during peak hours?**

**Expected Answer:**
- **Horizontal Scaling**: Auto-scaling groups for each microservice
- **Database Sharding**: Partition orders by user_id or location
- **Caching Strategy**: 
  - L1: Application cache for product data
  - L2: Redis for inventory and cart data
  - L3: CDN for static content
- **Load Balancing**: Geographic load balancing with region-specific deployments
- **Async Processing**: Use message queues for non-critical operations
- **Database Optimization**: Read replicas, connection pooling, query optimization

**Q: How would you ensure 99.9% uptime for Zepto?**

**Expected Answer:**
- **Circuit Breaker Pattern**: Prevent cascade failures
- **Redundancy**: Multi-AZ deployments with automatic failover
- **Health Checks**: Comprehensive monitoring with auto-recovery
- **Graceful Degradation**: Fallback to cached data when services are down
- **Blue-Green Deployment**: Zero-downtime deployments
- **Disaster Recovery**: Cross-region backups and recovery procedures

#### 2. **Real-time Features**

**Q: How would you implement real-time inventory updates across 1000+ warehouses?**

**Expected Answer:**
- **Event-Driven Architecture**: Kafka for real-time event streaming
- **CQRS Pattern**: Separate read/write models for inventory
- **WebSocket Connections**: Real-time updates to frontend
- **Database Design**: Optimistic locking with version control
- **Caching Strategy**: Redis for real-time inventory cache
- **Conflict Resolution**: Last-write-wins with compensation patterns

**Q: How would you implement real-time order tracking?**

**Expected Answer:**
- **Location Services**: GPS tracking for delivery partners
- **WebSocket/SSE**: Real-time updates to customers
- **Geofencing**: Automated status updates based on location
- **Event Sourcing**: Maintain complete order state history
- **Push Notifications**: Mobile notifications for status updates

#### 3. **Data Consistency**

**Q: How would you handle inventory reservation when multiple users try to buy the last item?**

**Expected Answer:**
- **Optimistic Locking**: Version-based concurrency control
- **Distributed Locking**: Redis-based locks for critical operations
- **Idempotency**: Ensure operations can be safely retried
- **Saga Pattern**: Manage distributed transactions
- **Compensation**: Automatic rollback on failures
- **Queue-based**: Process inventory updates sequentially

### Low Level Design Questions

#### 1. **Service Design**

**Q: Design the Order Service with all its components.**

**Expected Answer:**
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderCommand command = new PlaceOrderCommand(request);
        OrderResult result = commandHandler.execute(command);
        return ResponseEntity.ok(result.getOrder());
    }
}

@Service
public class OrderService {
    
    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        // Validate cart
        // Reserve inventory
        // Process payment
        // Create order
        // Send notifications
    }
}
```

**Q: How would you implement the pricing service with dynamic pricing?**

**Expected Answer:**
```java
@Component
public class PricingService {
    
    public Money calculatePrice(Product product, PricingContext context) {
        PricingStrategy strategy = strategyFactory.getStrategy(context);
        return strategy.calculatePrice(product, context);
    }
}

// Different pricing strategies
public class DemandBasedPricingStrategy implements PricingStrategy {
    // Implement surge pricing based on demand
}

public class InventoryBasedPricingStrategy implements PricingStrategy {
    // Implement pricing based on inventory levels
}
```

#### 2. **Database Schema Design**

**Q: Design the database schema for orders and inventory.**

**Expected Answer:**
```sql
-- Optimized for high-frequency updates
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, -- For optimistic locking
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory UNIQUE (product_id, warehouse_id)
);

-- Partitioned by date for performance
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);
```

### System Design Best Practices

#### 1. **Microservices Communication**

**Q: How would you ensure data consistency across microservices?**

**Expected Answer:**
- **Saga Pattern**: Choreography vs Orchestration
- **Event Sourcing**: Maintain audit trail of all changes
- **CQRS**: Separate read and write models
- **Eventual Consistency**: Accept temporary inconsistencies
- **Compensation**: Implement undo operations
- **Idempotency**: Ensure operations are safe to retry

#### 2. **Performance Optimization**

**Q: How would you optimize the system for low latency?**

**Expected Answer:**
- **Database Optimization**: Proper indexing, query optimization
- **Caching Strategy**: Multi-layer caching (Redis, Application, CDN)
- **Connection Pooling**: Efficient database connection management
- **Async Processing**: Move heavy operations to background
- **Geographic Distribution**: Deploy closer to users
- **Content Delivery**: Use CDN for static assets

## Implementation Guide

### Getting Started

#### 1. **Project Setup**
```bash
# Create microservices structure
mkdir zepto-system
cd zepto-system

# Create individual services
mkdir user-service order-service inventory-service payment-service
mkdir notification-service delivery-service pricing-service

# Setup infrastructure
mkdir infrastructure
mkdir infrastructure/docker
mkdir infrastructure/kubernetes
mkdir infrastructure/terraform
```

#### 2. **Docker Compose for Local Development**
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: zepto
      POSTGRES_USER: zepto
      POSTGRES_PASSWORD: zepto123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:6
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: admin123

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false

volumes:
  postgres_data:
```

#### 3. **Kubernetes Deployment**
```yaml
# k8s/order-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: zepto/order-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
```

### Testing Strategy

#### 1. **Unit Tests**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private InventoryService inventoryService;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void shouldPlaceOrderSuccessfully() {
        // Given
        PlaceOrderRequest request = createValidOrderRequest();
        when(inventoryService.reserveInventory(any())).thenReturn(true);
        
        // When
        Order order = orderService.placeOrder(request);
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(any(Order.class));
    }
}
```

#### 2. **Integration Tests**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class OrderServiceIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateOrderEndToEnd() {
        // Given
        PlaceOrderRequest request = createValidOrderRequest();
        
        // When
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/orders", request, OrderResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getOrderId()).isNotNull();
    }
}
```

### Monitoring and Observability

#### 1. **Metrics Collection**
```java
@Component
public class OrderMetrics {
    
    private final Counter orderCounter;
    private final Timer orderProcessingTime;
    private final Gauge activeOrders;
    
    public OrderMetrics(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("orders.placed")
            .description("Number of orders placed")
            .register(meterRegistry);
        
        this.orderProcessingTime = Timer.builder("orders.processing.time")
            .description("Order processing time")
            .register(meterRegistry);
            
        this.activeOrders = Gauge.builder("orders.active")
            .description("Number of active orders")
            .register(meterRegistry);
    }
    
    public void recordOrderPlaced() {
        orderCounter.increment();
    }
    
    public void recordProcessingTime(Duration duration) {
        orderProcessingTime.record(duration);
    }
}
```

#### 2. **Distributed Tracing**
```java
@RestController
public class OrderController {
    
    @PostMapping("/api/orders")
    @NewSpan("place-order")
    public ResponseEntity<OrderResponse> placeOrder(
        @RequestBody PlaceOrderRequest request) {
        
        Span span = tracer.nextSpan().name("order-validation");
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("user.id", request.getUserId());
            span.tag("order.items", String.valueOf(request.getItems().size()));
            
            OrderResponse response = orderService.placeOrder(request);
            return ResponseEntity.ok(response);
        } finally {
            span.end();
        }
    }
}
```

This comprehensive system design for Zepto covers all aspects from high-level architecture to implementation details, providing a solid foundation for building an ultra-fast grocery delivery platform. 