# 🚀 PentaChat Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**PentaChat** is a modern, high-performance messaging platform backend built with Spring Boot 3. It provides a robust foundation for real-time communication, featuring social networking capabilities, secure authentication, and a scalable architecture.

---

## ✨ Key Features

- **🔐 Secure Identity**: Full-featured authentication and authorization using Spring Security.
- **💬 Real-time Messaging**: Instant communication powered by WebSockets.
- **👥 Social Connectivity**: Friend request management, user profiles, and social interactions.
- **🏘️ Group Dynamics**: Scalable group chat functionality with administrative controls.
- **✉️ Automated Notifications**: Email integration for alerts and OTP verification.
- **🛡️ Data Integrity**: Strict validation and transactional consistency.
- **🌍 Multi-DB Support**: Configurable support for MySQL, SQL Server, and H2 (for testing).

---

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.5.10 |
| **Language** | Java 21 |
| **Security** | Spring Security |
| **Real-time** | Spring WebSocket |
| **Persistence** | Spring Data JPA (Hibernate) |
| **Database** | SQL Server (Primary), MySQL, H2 |
| **Mailing** | Spring Boot Starter Mail |
| **Utilities** | Lombok, Jakarta Validation |
| **Build Tool** | Maven |

---

## 🏗️ Architectural Overview

The project follows the **Controller-Service-Repository (CSR)** pattern to ensure clean separation of concerns and maintainability.

- **Controller**: REST Endpoints & Request Handling.
- **Service**: Business Logic & Transaction Management.
- **Repository**: Data Access Layer.
- **DTOs & Mappers**: Data transfer objects for API consistency and security.

### ⚙️ Project Conventions
This project follows strict development guidelines defined in [QuyUoc.md](file:///d:/NewFolder/Pentachat-be/QuyUoc.md). 
- **Database**: No foreign key constraints in the database; relationships are managed at the application layer using ID references.
- **API**: RESTful standards with a unified `ApiResponse` structure.
- **Dependency Injection**: Constructor injection is strictly enforced over field injection.

### Folder Structure
```text
src/main/java/com/hdtpt/pentachat/
├── identity/      # Auth & User Management
├── friend/        # Social Networking Logic
├── groups/        # Group Communication
├── message/       # Message Handling
├── websocket/     # RTC Configuration
├── security/      # Security Filters & Configs
├── config/        # Application Configuration
└── exception/     # Global Error Handling
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21 JDK
- Maven 3.x
- SQL Server (or update `application.properties` for MySQL/H2)

### Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/TranThanh-Hoai/Pentachat-be.git
   cd Pentachat-be
   ```

2. **Configure Database**
   Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost;databaseName=PentachatDB;...
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Configure Mail (Optional)**
   Update the mail settings to enable OTP and notifications:
   ```properties
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## 🧪 Testing
Run the comprehensive test suite to ensure stability:
```bash
mvn test
```

---

## 📖 API Documentation
Hệ thống tích hợp Swagger UI để hỗ trợ tra cứu và test API trực quan:
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Hướng dẫn chi tiết**: Xem tại [SWAGGER_GUIDE.md](file:///d:/NewFolder/Pentachat-be/SWAGGER_GUIDE.md)

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Developed with ❤️ by **TranThanh-Hoai**