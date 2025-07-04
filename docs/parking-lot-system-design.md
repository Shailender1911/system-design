# Parking Lot Management System Design

## Table of Contents
1. [Overview](#overview)
2. [High Level Design (HLD)](#high-level-design-hld)
3. [Low Level Design (LLD)](#low-level-design-lld)
4. [API Documentation](#api-documentation)
5. [Database Schema](#database-schema)
6. [Getting Started](#getting-started)
7. [Testing](#testing)

## Overview

The Parking Lot Management System is a comprehensive solution for managing parking facilities, vehicles, and payments. It provides real-time tracking of parking spots, automated fee calculation, and complete ticket management.

### Key Features
- **Multi-floor parking lots** with different spot types
- **Real-time availability** tracking
- **Automated fee calculation** based on duration
- **Multiple vehicle types** support (Motorcycle, Car, Truck)
- **Payment processing** and ticket management
- **RESTful APIs** with comprehensive documentation
- **Security** with role-based access control

### Business Requirements
- Support multiple parking lots with multiple floors
- Different spot types: Compact, Large, Motorcycle, Handicapped
- Real-time spot availability tracking
- Automated parking fee calculation
- Payment processing and receipt generation
- Search and reporting capabilities

## High Level Design (HLD)

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Parking Lot Management System                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   Web Client    │  │   Mobile App    │  │   Admin Panel   │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│            │                    │                    │           │
│            └────────────────────┼────────────────────┘           │
│                                 │                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    API Gateway                              │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                 │                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              REST Controllers                               │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │ │
│  │  │ Parking Lot     │  │    Vehicle      │  │   Payment   │  │ │
│  │  │ Controller      │  │   Controller    │  │ Controller  │  │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────┘  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                 │                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Service Layer                                │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │ │
│  │  │ Parking Lot     │  │    Ticket       │  │   Payment   │  │ │
│  │  │ Service         │  │   Service       │  │  Service    │  │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────┘  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                 │                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Repository Layer                               │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │ │
│  │  │ Parking Lot     │  │ Parking Spot    │  │   Ticket    │  │ │
│  │  │ Repository      │  │  Repository     │  │ Repository  │  │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────┘  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                 │                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    Database                                 │ │
│  │              (H2 In-Memory / PostgreSQL)                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

1. **Parking Lot Management**
   - Creation and management of parking lots
   - Floor and spot allocation
   - Availability tracking

2. **Vehicle Management**
   - Vehicle entry and exit processing
   - Spot assignment based on vehicle type
   - Real-time tracking

3. **Ticket Management**
   - Ticket generation and validation
   - Status tracking (Active, Completed, Cancelled)
   - Payment processing

4. **Payment System**
   - Fee calculation based on duration
   - Multiple payment methods support
   - Receipt generation

### Data Flow

1. **Vehicle Entry**
   ```
   Vehicle Arrives → Find Available Spot → Generate Ticket → Update Availability
   ```

2. **Vehicle Exit**
   ```
   Present Ticket → Calculate Fee → Process Payment → Release Spot → Update Availability
   ```

## Low Level Design (LLD)

### Entity Relationship Diagram

```
┌─────────────────┐    1:N    ┌─────────────────┐    1:N    ┌─────────────────┐
│   ParkingLot    │───────────│  ParkingFloor   │───────────│   ParkingSpot   │
│─────────────────│           │─────────────────│           │─────────────────│
│ id (PK)         │           │ id (PK)         │           │ id (PK)         │
│ name            │           │ floorNumber     │           │ spotNumber      │
│ location        │           │ totalSpots      │           │ spotType        │
│ totalFloors     │           │ availableSpots  │           │ spotStatus      │
│ totalSpots      │           │ parkingLotId(FK)│           │ parkingFloorId  │
│ availableSpots  │           │ createdAt       │           │ createdAt       │
│ isActive        │           │ updatedAt       │           │ updatedAt       │
│ createdAt       │           └─────────────────┘           └─────────────────┘
│ updatedAt       │                                                │
└─────────────────┘                                                │
        │                                                          │
        │                                                          │
        │            ┌─────────────────┐                          │
        │     1:N    │  ParkingTicket  │    N:1                   │
        └────────────│─────────────────│──────────────────────────┘
                     │ id (PK)         │
                     │ ticketNumber    │
                     │ licensePlate    │
                     │ vehicleType     │
                     │ entryTime       │
                     │ exitTime        │
                     │ amountPaid      │
                     │ paymentStatus   │
                     │ ticketStatus    │
                     │ parkingLotId(FK)│
                     │ parkingSpotId   │
                     │ createdAt       │
                     │ updatedAt       │
                     └─────────────────┘
```

### Core Classes

#### 1. ParkingLot Entity
```java
@Entity
public class ParkingLot {
    private Long id;
    private String name;
    private String location;
    private Integer totalFloors;
    private Integer spotsPerFloor;
    private Integer totalSpots;
    private Integer availableSpots;
    private Boolean isActive;
    private List<ParkingFloor> floors;
    private List<ParkingTicket> tickets;
    
    // Business methods
    public boolean hasAvailableSpots();
    public void reserveSpot();
    public void releaseSpot();
}
```

#### 2. ParkingSpot Entity
```java
@Entity
public class ParkingSpot {
    private Long id;
    private String spotNumber;
    private SpotType spotType;
    private SpotStatus spotStatus;
    private ParkingFloor parkingFloor;
    
    // Business methods
    public boolean isAvailable();
    public void occupy();
    public void release();
    public boolean canAccommodateVehicle(VehicleType vehicleType);
}
```

#### 3. ParkingTicket Entity
```java
@Entity
public class ParkingTicket {
    private Long id;
    private String ticketNumber;
    private String licensePlate;
    private VehicleType vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal amountPaid;
    private PaymentStatus paymentStatus;
    private TicketStatus ticketStatus;
    private ParkingLot parkingLot;
    private ParkingSpot parkingSpot;
    
    // Business methods
    public boolean isActive();
    public boolean isPaid();
    public void complete();
}
```

### Service Layer Design

#### ParkingLotService
```java
public interface ParkingLotService {
    ParkingLotDTO createParkingLot(CreateParkingLotRequest request);
    ParkingLotDTO getParkingLotById(Long id);
    List<ParkingLotDTO> getAllParkingLots();
    List<ParkingLotDTO> getParkingLotsWithAvailableSpots();
    ParkingTicketDTO parkVehicle(ParkVehicleRequest request);
    ParkingTicketDTO exitVehicle(String ticketNumber);
    ParkingTicketDTO processPayment(String ticketNumber, BigDecimal amount);
    BigDecimal calculateParkingFee(String ticketNumber);
}
```

### Algorithm Design

#### 1. Spot Assignment Algorithm
```java
public ParkingSpot findAvailableSpot(Long parkingLotId, VehicleType vehicleType) {
    // Priority order: Best fit first
    // 1. Try to find exact match (motorcycle -> motorcycle spot)
    // 2. Find larger spot if exact match not available
    // 3. Prioritize lower floors and lower spot numbers
    
    return parkingSpotRepository.findFirstAvailableSpotForVehicleType(
        parkingLotId, vehicleType, SpotStatus.AVAILABLE);
}
```

#### 2. Fee Calculation Algorithm
```java
public BigDecimal calculateParkingFee(String ticketNumber) {
    // Pricing strategy:
    // - $5 per hour
    // - $25 per day (24+ hours)
    // - $2 minimum charge
    // - Round up to next hour
    
    Duration duration = Duration.between(entryTime, exitTime);
    long hours = duration.toHours();
    if (duration.toMinutes() % 60 > 0) {
        hours++; // Round up
    }
    
    BigDecimal fee;
    if (hours >= 24) {
        long days = hours / 24;
        long remainingHours = hours % 24;
        fee = DAILY_RATE.multiply(BigDecimal.valueOf(days))
                .add(HOURLY_RATE.multiply(BigDecimal.valueOf(remainingHours)));
    } else {
        fee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));
    }
    
    return fee.max(MINIMUM_CHARGE);
}
```

## API Documentation

### Base URL
```
http://localhost:8080/api/parking-lot
```

### Authentication
- Basic Authentication (admin/admin123)
- Role-based access control
- Admin role required for creating parking lots

### Endpoints

#### 1. Create Parking Lot
```http
POST /api/parking-lot
Content-Type: application/json
Authorization: Basic admin:admin123

{
    "name": "Downtown Parking",
    "location": "123 Main St",
    "totalFloors": 3,
    "spotsPerFloor": 20
}
```

#### 2. Park Vehicle
```http
POST /api/parking-lot/park
Content-Type: application/json

{
    "licensePlate": "ABC-1234",
    "vehicleType": "CAR",
    "parkingLotId": 1
}
```

#### 3. Exit Vehicle
```http
POST /api/parking-lot/exit/TKT-12345678
```

#### 4. Calculate Fee
```http
GET /api/parking-lot/fee/TKT-12345678
```

#### 5. Get Available Parking Lots
```http
GET /api/parking-lot/available
```

### Response Format
```json
{
    "success": true,
    "message": "Operation successful",
    "data": { ... },
    "timestamp": "2024-01-01T12:00:00"
}
```

## Database Schema

### Tables

#### parking_lot
```sql
CREATE TABLE parking_lot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    location VARCHAR(255) NOT NULL,
    total_floors INT NOT NULL,
    spots_per_floor INT NOT NULL,
    total_spots INT NOT NULL,
    available_spots INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### parking_floor
```sql
CREATE TABLE parking_floor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_number INT NOT NULL,
    total_spots INT NOT NULL,
    available_spots INT NOT NULL,
    parking_lot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lot(id)
);
```

#### parking_spot
```sql
CREATE TABLE parking_spot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spot_number VARCHAR(50) NOT NULL,
    spot_type ENUM('COMPACT', 'LARGE', 'MOTORCYCLE', 'HANDICAPPED') NOT NULL,
    spot_status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE') NOT NULL,
    parking_floor_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (parking_floor_id) REFERENCES parking_floor(id)
);
```

#### parking_ticket
```sql
CREATE TABLE parking_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    license_plate VARCHAR(50) NOT NULL,
    vehicle_type ENUM('MOTORCYCLE', 'CAR', 'TRUCK') NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    amount_paid DECIMAL(10,2),
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') NOT NULL,
    ticket_status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL,
    parking_lot_id BIGINT NOT NULL,
    parking_spot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lot(id),
    FOREIGN KEY (parking_spot_id) REFERENCES parking_spot(id)
);
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Running the Application

1. **Clone the repository**
```bash
git clone <repository-url>
cd System-Design
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
- API Documentation: http://localhost:8080/swagger-ui.html
- H2 Database Console: http://localhost:8080/h2-console
- Health Check: http://localhost:8080/actuator/health

### Configuration

To run the parking lot system:
1. Set `spring.profiles.active=parking-lot` in `application.properties`
2. Start the application
3. Use the API endpoints to interact with the system

## Testing

### Manual Testing Workflow

1. **Create a parking lot**
```bash
curl -X POST http://localhost:8080/api/parking-lot \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "name": "Test Parking Lot",
    "location": "Test Location",
    "totalFloors": 2,
    "spotsPerFloor": 10
  }'
```

2. **Park a vehicle**
```bash
curl -X POST http://localhost:8080/api/parking-lot/park \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "TEST-123",
    "vehicleType": "CAR",
    "parkingLotId": 1
  }'
```

3. **Check parking fee**
```bash
curl -X GET http://localhost:8080/api/parking-lot/fee/TKT-12345678
```

4. **Exit vehicle**
```bash
curl -X POST http://localhost:8080/api/parking-lot/exit/TKT-12345678
```

### Test Scenarios

1. **Happy Path**: Park vehicle → Calculate fee → Exit vehicle
2. **Error Handling**: Try to park already parked vehicle
3. **Edge Cases**: Park when no spots available
4. **Concurrent Access**: Multiple vehicles parking simultaneously

## Scalability Considerations

### Current Implementation
- In-memory H2 database for development
- Single instance application
- Basic authentication

### Production Enhancements
- **Database**: PostgreSQL with connection pooling
- **Caching**: Redis for frequently accessed data
- **Load Balancing**: Multiple application instances
- **Monitoring**: Prometheus + Grafana
- **Security**: JWT tokens, OAuth2
- **Message Queue**: RabbitMQ for async processing

### Performance Optimizations
- Database indexing on frequently queried columns
- Caching of parking lot availability
- Async processing for non-critical operations
- Connection pooling and query optimization

## Future Enhancements

1. **Real-time Updates**: WebSocket for live availability updates
2. **Mobile App**: React Native mobile application
3. **Payment Integration**: Stripe, PayPal integration
4. **Analytics**: Reporting dashboard for parking statistics
5. **IoT Integration**: Sensor-based spot detection
6. **Reservation System**: Advance booking capabilities
7. **Dynamic Pricing**: Peak hour pricing strategy 