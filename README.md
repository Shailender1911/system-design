# System Design Problems and Solutions

A comprehensive Spring Boot application implementing multiple system design problems with detailed High Level Design (HLD) and Low Level Design (LLD) documentation.

## 🌟 Overview

This project contains **10-20 system design problems** with complete implementations including:
- **High Level Design (HLD)** with system architecture diagrams
- **Low Level Design (LLD)** with detailed class diagrams and algorithms
- **Working code** with full functionality
- **RESTful APIs** with comprehensive documentation
- **Database schemas** with proper relationships
- **Profile-based configuration** for easy switching between designs

## 🎯 Available System Designs

### 1. ✅ Parking Lot Management System
- **Status**: Fully Implemented
- **Profile**: `parking-lot`
- **Features**: Multi-floor parking, real-time availability, automated fee calculation
- **Documentation**: [Parking Lot System Design](docs/parking-lot-system-design.md)

### 2. 🚀 Zepto (Quick Commerce) System
- **Status**: Configured
- **Profile**: `zepto`
- **Features**: 10-minute delivery, inventory management, real-time tracking
- **Documentation**: Coming soon

### 3. 🔗 URL Shortener Service
- **Status**: Planned
- **Profile**: `url-shortener`
- **Features**: Short URL generation, analytics, custom domains

### 4. 💬 Chat System
- **Status**: Planned
- **Profile**: `chat-system`
- **Features**: Real-time messaging, group chats, file sharing

### 5. 🍔 Food Delivery Platform
- **Status**: Planned
- **Profile**: `food-delivery`
- **Features**: Restaurant management, order tracking, delivery optimization

### 6. 📱 Social Media Platform
- **Status**: Planned
- **Profile**: `social-media`
- **Features**: Posts, feeds, notifications, user interactions

### 7. 🎥 Video Streaming Service
- **Status**: Planned
- **Profile**: `video-streaming`
- **Features**: Video upload, encoding, CDN, recommendations

### 8. 🛒 E-commerce Platform
- **Status**: Planned
- **Profile**: `e-commerce`
- **Features**: Product catalog, shopping cart, payment processing

### 9. 🏦 Banking System
- **Status**: Planned
- **Profile**: `banking`
- **Features**: Account management, transactions, fraud detection

### 10. 🚗 Ride Sharing Service
- **Status**: Planned
- **Profile**: `ride-sharing`
- **Features**: Ride matching, route optimization, pricing

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/shailenderkumar/System-Design.git
cd System-Design
```

2. **Build the project**
```bash
mvn clean install
```

3. **Choose a system design to run**
Edit `src/main/resources/application.properties`:
```properties
# Change this to run different system designs
spring.profiles.active=parking-lot
```

4. **Run the application**
```bash
mvn spring-boot:run
```

5. **Access the application**
- 📚 **API Documentation**: http://localhost:8080/swagger-ui.html
- 🗄️ **Database Console**: http://localhost:8080/h2-console
- 📊 **Health Check**: http://localhost:8080/actuator/health
- 📖 **API Docs**: http://localhost:8080/api-docs

## 🎮 Usage Examples

### Parking Lot System

#### 1. Create a Parking Lot
```bash
curl -X POST http://localhost:8080/api/parking-lot \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "name": "Downtown Parking",
    "location": "123 Main St",
    "totalFloors": 3,
    "spotsPerFloor": 20
  }'
```

#### 2. Park a Vehicle
```bash
curl -X POST http://localhost:8080/api/parking-lot/park \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC-1234",
    "vehicleType": "CAR",
    "parkingLotId": 1
  }'
```

#### 3. Check Available Parking Lots
```bash
curl -X GET http://localhost:8080/api/parking-lot/available
```

#### 4. Calculate Parking Fee
```bash
curl -X GET http://localhost:8080/api/parking-lot/fee/TKT-12345678
```

#### 5. Exit Vehicle
```bash
curl -X POST http://localhost:8080/api/parking-lot/exit/TKT-12345678
```

## 📋 System Design Patterns Used

### Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **DTO Pattern**: Data transfer objects
- **Builder Pattern**: Object construction
- **Strategy Pattern**: Algorithm encapsulation
- **Observer Pattern**: Event handling

### Spring Boot Features
- **Profiles**: Environment-specific configurations
- **JPA**: Database operations
- **Validation**: Input validation
- **Security**: Authentication and authorization
- **Caching**: Performance optimization
- **Actuator**: Application monitoring

## 🏗️ Project Structure

```
System-Design/
├── src/main/java/com/system/design/
│   ├── System/Design/
│   │   └── SystemDesignApplication.java
│   ├── common/
│   │   ├── config/
│   │   │   └── CommonConfig.java
│   │   ├── dto/
│   │   │   └── ApiResponse.java
│   │   └── exception/
│   │       ├── BusinessException.java
│   │       ├── GlobalExceptionHandler.java
│   │       └── ResourceNotFoundException.java
│   └── designs/
│       ├── parkinglot/
│       │   ├── controller/
│       │   │   └── ParkingLotController.java
│       │   ├── service/
│       │   │   ├── ParkingLotService.java
│       │   │   └── ParkingLotServiceImpl.java
│       │   ├── repository/
│       │   │   ├── ParkingLotRepository.java
│       │   │   ├── ParkingSpotRepository.java
│       │   │   └── ParkingTicketRepository.java
│       │   ├── entity/
│       │   │   ├── ParkingLot.java
│       │   │   ├── ParkingFloor.java
│       │   │   ├── ParkingSpot.java
│       │   │   ├── ParkingTicket.java
│       │   │   └── [Enums]
│       │   └── dto/
│       │       ├── ParkingLotDTO.java
│       │       ├── ParkingTicketDTO.java
│       │       └── [Request DTOs]
│       ├── zepto/
│       │   └── [Similar structure]
│       └── [Other system designs]
├── src/main/resources/
│   ├── application.properties
│   ├── application-parking-lot.properties
│   ├── application-zepto.properties
│   └── [Other profile configs]
├── docs/
│   ├── parking-lot-system-design.md
│   └── [Other documentation]
└── README.md
```

## 🔧 Configuration

### Switching Between System Designs

1. **Edit application.properties**
```properties
spring.profiles.active=parking-lot    # Change to desired profile
```

2. **Available Profiles**
- `parking-lot` - Parking Lot Management System
- `zepto` - Quick Commerce Platform
- `url-shortener` - URL Shortening Service
- `chat-system` - Real-time Chat System
- `food-delivery` - Food Delivery Platform

### Database Configuration

#### Development (H2 In-Memory)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
```

#### Production (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/systemdesign
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 📊 Performance & Scalability

### Current Implementation
- **Database**: H2 in-memory for development
- **Caching**: Caffeine cache for frequently accessed data
- **Security**: Basic authentication
- **Monitoring**: Spring Boot Actuator

### Production Enhancements
- **Database**: PostgreSQL with connection pooling
- **Caching**: Redis for distributed caching
- **Load Balancing**: Multiple application instances
- **Monitoring**: Prometheus + Grafana
- **Security**: JWT tokens, OAuth2
- **Message Queue**: RabbitMQ for async processing

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn integration-test
```

### Manual Testing
1. Start the application
2. Open Swagger UI: http://localhost:8080/swagger-ui.html
3. Test API endpoints using the interactive documentation

## 📈 Monitoring & Observability

### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Application Info
```bash
curl http://localhost:8080/actuator/info
```

## 🔒 Security

### Authentication
- **Default Credentials**: admin/admin123
- **Basic Authentication** for admin endpoints
- **Role-based access control**

### API Security
- Input validation using Bean Validation
- SQL injection prevention using JPA
- XSS protection with proper response encoding
- CSRF protection for state-changing operations

## 🌐 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### API Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-01T12:00:00",
  "error": null,
  "path": "/api/parking-lot"
}
```

## 🤝 Contributing

### Adding a New System Design

1. **Create the package structure**
```
src/main/java/com/system/design/designs/[system-name]/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

2. **Create profile configuration**
```
src/main/resources/application-[system-name].properties
```

3. **Add documentation**
```
docs/[system-name]-system-design.md
```

4. **Update README.md** with the new system design

### Development Guidelines
- Follow Spring Boot best practices
- Use proper layered architecture
- Implement comprehensive error handling
- Add proper logging
- Write unit and integration tests
- Document APIs with OpenAPI annotations

## 📚 Learning Resources

### System Design Concepts
- **High Level Design**: System architecture, scalability, reliability
- **Low Level Design**: Class diagrams, database schema, algorithms
- **Design Patterns**: Common solutions to recurring problems
- **Microservices**: Service decomposition strategies
- **Database Design**: Normalization, indexing, partitioning

### Spring Boot Features
- **Spring Data JPA**: Database operations
- **Spring Security**: Authentication and authorization
- **Spring Cache**: Caching abstraction
- **Spring Profiles**: Environment-specific configurations
- **Spring Actuator**: Application monitoring

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
```bash
# Change port in application.properties
server.port=8081
```

2. **Database Connection Issues**
```bash
# Check H2 console at http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (empty)
```

3. **Profile Not Loading**
```bash
# Verify profile name in application.properties
spring.profiles.active=parking-lot
```

## 📞 Support

For questions, issues, or contributions:
- **GitHub Issues**: [Create an issue](https://github.com/shailenderkumar/System-Design/issues)
- **Email**: shailender.kumar@example.com
- **LinkedIn**: [Shailender Kumar](https://linkedin.com/in/shailender-kumar)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- System Design Community for inspiration
- Open Source Contributors for tools and libraries

---

**Made with ❤️ by [Shailender Kumar](https://github.com/shailenderkumar)** 