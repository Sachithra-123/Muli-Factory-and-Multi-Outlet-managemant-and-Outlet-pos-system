# Project Documentation: Factory Management System

This document provides a comprehensive overview of the Factory Management System (FMS), explaining the project structure, technical architecture, and the purpose of every key file.

---

## 1. Technical Architecture Summary
- **Backend Framework**: Spring Boot 3.2.5
- **Persistence Layer**: Spring Data JPA with Hibernate
- **Database**: MySQL 8.0+
- **Frontend Engine**: Thymeleaf (Server-side rendering)
- **Design System**: SaaS Professional (Custom CSS with Liquid Animations)
- **Security**: Spring Security 6 (Role-based access: ADMIN, CASHIER)

---

## 2. Backend Structure (Java)
Located in: `src/main/java/com/factory/management/`

### 📦 Entity (Database Models)
These files define the database schema and object relationships.
- `Employee.java`: Stores user data, credentials, and roles.
- `Product.java`: Core product details (name, price, category).
- `Factory.java`: Manufacturing unit locations and details.
- `Outlet.java`: Retail location details.
- `FactoryStock.java`: Tracks inventory levels within specific factories.
- `OutletStock.java`: Tracks inventory levels within retail outlets.
- `Sale.java`: Records transaction headers (date, total, outlet).
- `SaleItem.java`: Records specific products sold in each transaction.
- `StockTransfer.java`: Records the movement of goods between locations.

### 📦 Repository (Data Access)
Interfaces that provide CRUD operations using Spring Data JPA.
- `EmployeeRepository.java`
- `ProductRepository.java`
- `FactoryRepository.java`
- `SaleRepository.java`
- *...and repositories for all other entities.*

### 📦 Service (Business Logic)
Contains the "brains" of the application—calculating logic, stock updates, and security checks.
- `ProductService.java`: Logic for adding/inventory checking.
- `SaleService.java`: Handles the complex process of recording a sale and deducting stock simultaneously.
- `StockTransferService.java`: Manages the atomic transfer of goods between factory and outlet.
- `EmployeeService.java`: Handles user registration and profiling.

### 📦 Controller (Web Handlers)
Maps incoming web requests to the correct logic and returns the HTML views.
- `DashboardController.java`: Calculates and displays analytics for the main home page.
- `ProductController.java`: Handles product management (Add/Edit/Delete).
- `SaleController.java`: Manages the POS (Point of Sale) interface and sales history.
- `StockTransferController.java`: Handles the UI for moving stock.

### 📦 Security
- `WebSecurityConfig.java`: Configures URL permissions, login/logout behavior, and password encryption.
- `CustomUserDetailsService.java`: Loads user data during the login process.

---

## 3. Frontend & Resources
Located in: `src/main/resources/`

### 🎨 Static Resources
- `static/css/style.css`: **The Visual Engine**. Contains the entire "SaaS Professional" design system, including the Bento Grid layout, glass effects, and background animations.
- `static/js/app.js`: Handles UI interactions like sidebar toggling and basic validation.

### 📄 Templates (Thymeleaf HTML)
Divided into modules for organization:
- `dashboard/index.html`: The main analytics hub with Chart.js integration.
- `employee/`: Forms and lists for HR management.
- `product/`: Inventory management interfaces.
- `pos/`:
    - `sale.html`: The interactive Point of Sale interface.
    - `bill.html`: The printable invoice template.
- `report/`: Advanced data views for Sales, Stock, and Transfers.
- `fragments/`: Reusable UI pieces like the **Sidebar** and **Navbar**.

---

## 4. Configuration
- `pom.xml`: The Maven manifest. Defines all dependencies (Spring Web, JPA, Security, MySQL, Lombok).
- `application.properties`: Connects the app to the MySQL database and sets server ports.
- `mvnw` / `mvnw.cmd`: Maven wrapper for running the project without a manual installation.

---

## 5. Design Philosophy: "SaaS Professional"
The current UI is built on four pillars:
1. **Bento Grid**: Modular information architecture.
2. **Authority Colors**: A palette of Navy (#0f172a) and Emerald (#10b981).
3. **Subtle Motion**: Slow-moving background auras to create a premium depth.
4. **Data First**: Ultra-legible typography and high-contrast tables.

---

## 6. How to Run
1. Start MySQL (`MYSQL80` service).
2. Use the terminal in the root folder: `./mvnw spring-boot:run`.
3. Open `http://localhost:8080` in your browser.
