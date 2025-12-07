# 🛒 SmartShop - B2B E-Commerce Platform

A comprehensive Spring Boot REST API for B2B e-commerce operations with advanced features including multi-payment systems, automatic loyalty management, and Moroccan tax compliance.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Business Logic](#-business-logic)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 🔐 **Session-Based Authentication** - Secure login/logout with role-based access (ADMIN/CLIENT)
- 👥 **User & Client Management** - Separate user authentication and client business profiles
- 📦 **Product Catalog** - Product management with stock tracking and soft delete
- 🛍️ **Order Management** - Complete order lifecycle from creation to confirmation
- 💳 **Multi-Payment System** - Support for ESPECES, CHEQUE, TRAITE, and VIREMENT
- 🎁 **Automatic Loyalty Program** - 3-tier system (BASIC, SILVER, GOLD, PLATINUM)
- 📊 **Discount Engine** - Loyalty-based and promotional discounts
- 🧾 **Moroccan Tax Compliance** - TVA (20%) calculation and cash limit enforcement (20,000 DH)

### Technical Features
- ✅ **RESTful API** - Clean, intuitive endpoints following REST principles
- ✅ **Data Validation** - Request validation with clear error messages
- ✅ **Soft Delete** - Products marked as deleted, never removed from database
- ✅ **Pagination** - Efficient product listing with pagination support
- ✅ **Historical Data** - Order items preserve pricing at time of purchase
- ✅ **Comprehensive Testing** - 28 unit, integration, and controller tests (100% passing)
- ✅ **API Documentation** - Interactive Swagger UI at `/swagger-ui.html`

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern Java LTS version
- **Spring Boot 3.4.0** - Enterprise application framework
- **Spring Data JPA** - Data persistence with Hibernate
- **Spring Web** - RESTful web services
- **PostgreSQL 18** - Production database
- **H2 Database** - In-memory database for testing

### Tools & Libraries
- **Lombok** - Reduce boilerplate code
- **Springdoc OpenAPI** - API documentation (Swagger)
- **Spring Dotenv** - Environment variable management
- **Maven** - Dependency management and build tool
- **JUnit 5 & Mockito** - Testing framework

---



## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/SmartShopV2.git
cd SmartShopV2
```

### 2. Create PostgreSQL Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE smartshopv2;

-- Create user (optional)
CREATE USER smartshop WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE smartshopv2 TO smartshop;
```

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=smartshopv2
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Server Configuration
SERVER_PORT=8080

# Session Configuration
SESSION_TIMEOUT=30m
SESSION_COOKIE_NAME=SMARTSHOP_SESSION

# Application
APP_NAME=SmartShopV2
```


### 4. Build the Project

```bash
# Windows
.\mvnw clean install

# Linux/Mac
./mvnw clean install
```

---

## ⚙️ Configuration

### application.yml

The application uses YAML configuration with environment variable placeholders:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: update  # Creates/updates tables automatically
    show-sql: false     # Set to true for debugging
```

### Default Admin User

On first startup, a default admin account is created:
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `ADMIN`



---

## 🏃 Running the Application

### Development Mode

```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Production Mode

```bash
# Build JAR
.\mvnw clean package -DskipTests

# Run JAR
java -jar target/SmartShopV2-0.0.1-SNAPSHOT.jar
```

### Verify Application is Running

```bash
# Health check
curl http://localhost:8080/api/auth/session

# Should return 401 (not authenticated) - means API is working
```

---

## 📚 API Documentation

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Quick API Reference

#### Authentication
```http
POST   /api/auth/login      # Login
POST   /api/auth/logout     # Logout
GET    /api/auth/session    # Get current session
```

#### Products
```http
GET    /api/products                    # List all products (paginated)
GET    /api/products/{id}               # Get product by ID
POST   /api/products/create             # Create product (ADMIN only)
PUT    /api/products/{id}               # Update product (ADMIN only)
DELETE /api/products/{id}               # Soft delete product (ADMIN only)
```

#### Clients
```http
GET    /api/clients                     # List all clients (ADMIN only)
GET    /api/clients/{id}                # Get client by ID
POST   /api/clients/register            # Register new client
```

#### Orders
```http
POST   /api/orders/create               # Create new order
POST   /api/orders/{id}/confirm         # Confirm order (deduct stock)
GET    /api/orders/{id}                 # Get order details
GET    /api/orders/client/{clientId}    # Get client's orders
```

#### Payments
```http
POST   /api/payments/add                # Add payment to order
POST   /api/payments/{id}/encaisser     # Mark payment as cashed
POST   /api/payments/{id}/rejeter       # Reject payment
GET    /api/payments/order/{orderId}    # Get order payments
```

### Example Requests

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### Create Order
```bash
curl -X POST http://localhost:8080/api/orders/create \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "promoCode": "PROMO2024"
  }'
```

---

## 🧪 Testing

### Run All Tests

```bash
.\mvnw test
```

### Run Specific Test Class

```bash
.\mvnw test -Dtest=AuthServiceTest
.\mvnw test -Dtest=OrderServiceTest
.\mvnw test -Dtest=PaymentServiceTest
```

### Test Coverage

- **28 Tests** - 100% passing
- **Unit Tests** - Service layer with mocks (AuthServiceTest)
- **Integration Tests** - Full stack with H2 database (OrderServiceTest, PaymentServiceTest)
- **Controller Tests** - HTTP endpoint testing (AuthControllerTest)

### Test Reports

After running tests, view detailed reports at:
```
target/surefire-reports/index.html
```

---

## 📁 Project Structure

```
SmartShopV2/
├── src/
│   ├── main/
│   │   ├── java/org/example/smartshopv2/
│   │   │   ├── config/              # Application configuration
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/          # REST API endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ClientController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── ProductController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── OrderRequest.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   ├── PaymentRequest.java
│   │   │   │   └── PaymentResponse.java
│   │   │   ├── entity/              # JPA Entities
│   │   │   │   ├── Client.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Product.java
│   │   │   │   └── User.java
│   │   │   ├── enums/               # Enumerations
│   │   │   │   ├── LoyaltyLevel.java
│   │   │   │   ├── OrderStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   └── Role.java
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ClientService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   └── ProductService.java
│   │   │   └── SmartShopV2Application.java
│   │   └── resources/
│   │       ├── application.yml      # Main configuration
│   │       └── application-test.yml # Test configuration (H2)
│   └── test/
│       └── java/org/example/smartshopv2/
│           ├── controller/
│           │   └── AuthControllerTest.java
│           ├── service/
│           │   ├── AuthServiceTest.java
│           │   ├── OrderServiceTest.java
│           │   └── PaymentServiceTest.java
│           └── SmartShopV2ApplicationTests.java
├── .env                             # Environment variables (DO NOT COMMIT)
├── .env.example                     # Environment template
├── .gitignore                       # Git ignore rules
├── pom.xml                          # Maven dependencies
├── mvnw / mvnw.cmd                  # Maven wrapper
└── README.md                        # This file
```

---

## 💼 Business Logic

### Loyalty System

Automatic tier upgrades based on spending:

| Level | Min Spent | Discount % | Min Order for Discount |
|-------|-----------|------------|------------------------|
| BASIC | 0 DH | 0% | - |
| SILVER | 10,000 DH | 5% | 500 DH |
| GOLD | 50,000 DH | 10% | 800 DH |
| PLATINUM | 100,000 DH | 15% | 1,000 DH |

### Order Flow

```
1. Create Order (PENDING)
   ├── Calculate subtotal (sum of items)
   ├── Apply loyalty discount (if eligible)
   ├── Apply promo code (if valid)
   ├── Calculate TVA (20%)
   └── Set montantRestant = totalTTC

2. Add Payments
   ├── Validate payment amount ≤ montantRestant
   ├── ESPECES: Check ≤ 20,000 DH (legal limit)
   ├── Update montantRestant
   └── Set payment status (ENCAISSE or EN_ATTENTE)

3. Confirm Order
   ├── Check montantRestant = 0
   ├── Validate stock availability
   ├── Deduct stock quantities
   ├── Update order status to CONFIRMED
   └── Update client statistics
```

### Payment Types

| Type | Status on Creation | Cash Limit | Notes |
|------|-------------------|------------|-------|
| ESPECES | ENCAISSE | 20,000 DH | Immediate, Art. 193 CGI |
| CHEQUE | EN_ATTENTE | No limit | Requires encaissement |
| TRAITE | EN_ATTENTE | No limit | Requires encaissement |
| VIREMENT | EN_ATTENTE | No limit | Requires encaissement |

### Tax Calculation

```
Subtotal HT = Sum of (quantity × price) for all items
Discount = Loyalty discount + Promo code discount
Amount After Discount = Subtotal HT - Discount
TVA (20%) = Amount After Discount × 0.20
Total TTC = Amount After Discount + TVA
```

---

## 🔒 Security Best Practices

### Environment Variables
- ✅ Never commit `.env` file
- ✅ Use `.env.example` for documentation
- ✅ No fallback values for sensitive data in `application.yml`
- ✅ Fail fast if required variables are missing

### Authentication
- ✅ Session-based authentication with HttpOnly cookies
- ✅ Role-based access control (ADMIN/CLIENT)
- ✅ Consistent error messages (no username enumeration)
- ✅ Change default admin password in production

### API Security
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ CORS configuration for cross-origin requests
- ✅ Session timeout (30 minutes default)

---

## 🐛 Troubleshooting

### Application won't start

**Issue**: `Failed to configure a DataSource`
```
Solution: Check .env file exists and has correct database credentials
```

**Issue**: `Connection refused`
```
Solution: Ensure PostgreSQL is running on specified host/port
```

### Tests failing

**Issue**: Integration tests fail
```
Solution: Ensure H2 dependency is in pom.xml (scope: test)
```

### API returns 401

**Issue**: Unauthorized on valid requests
```
Solution: Login first to create session, send session cookie with requests
```

---

## 🤝 Contributing

### Workflow

1. Fork the repository
2. Create feature branch (`git checkout -b feature/SMART-XXX-amazing-feature`)
3. Commit changes (`git commit -m 'feat: add amazing feature'`)
4. Push to branch (`git push origin feature/SMART-XXX-amazing-feature`)
5. Open Pull Request

### Commit Convention

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example**:
```
feat(order): add promo code validation

- Validate promo code format
- Check expiration date
- Apply discount if valid

SMART-123
```

---







