# Factory & Outlet Management System — Implementation Plan

## Overview

A medium-scale enterprise web application for an HND IT final-year project. The system manages a small factory and its outlets, covering product management, inventory, POS sales, stock transfers, employee records, and basic reporting — all through a clean web UI using **Spring Boot + MySQL + Thymeleaf**.

---

## Tech Stack Summary

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.x, Spring MVC, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Frontend | Thymeleaf, Bootstrap 5, HTML/CSS, JavaScript |
| Build Tool | Maven |
| Auth | Spring Security + BCrypt |
| Reports | HTML-based print reports (or optional PDF via iText) |

---

## Project Structure

```
finalproject2/
├── src/main/java/com/factory/
│   ├── FactoryManagementApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── DashboardController.java
│   │   ├── ProductController.java
│   │   ├── FactoryStockController.java
│   │   ├── OutletController.java
│   │   ├── StockTransferController.java
│   │   ├── SaleController.java
│   │   ├── EmployeeController.java
│   │   └── ReportController.java
│   ├── dto/
│   │   ├── UserDTO.java
│   │   ├── ProductDTO.java
│   │   ├── SaleDTO.java
│   │   ├── SaleItemDTO.java
│   │   └── StockTransferDTO.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Product.java
│   │   ├── FactoryStock.java
│   │   ├── Outlet.java
│   │   ├── OutletStock.java
│   │   ├── StockTransfer.java
│   │   ├── Employee.java
│   │   ├── Sale.java
│   │   └── SaleItem.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── FactoryStockRepository.java
│   │   ├── OutletRepository.java
│   │   ├── OutletStockRepository.java
│   │   ├── StockTransferRepository.java
│   │   ├── EmployeeRepository.java
│   │   ├── SaleRepository.java
│   │   └── SaleItemRepository.java
│   └── service/
│       ├── UserService.java
│       ├── ProductService.java
│       ├── FactoryStockService.java
│       ├── OutletService.java
│       ├── StockTransferService.java
│       ├── SaleService.java
│       ├── EmployeeService.java
│       └── ReportService.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/
│   │   ├── css/style.css
│   │   └── js/pos.js
│   └── templates/
│       ├── layout/base.html
│       ├── auth/login.html
│       ├── dashboard/index.html
│       ├── product/list.html, form.html
│       ├── stock/factory.html
│       ├── outlet/list.html, form.html
│       ├── transfer/list.html, form.html
│       ├── pos/sale.html, bill.html
│       ├── employee/list.html, form.html
│       └── report/sales.html, stock.html, transfer.html
└── pom.xml
```

---

## Database Schema

### Tables and Relationships

```sql
-- Core tables
users (id, username, password, enabled, role_id)
roles (id, name)                                      -- ADMIN, CASHIER

-- Product catalog
products (id, name, description, unit_price, category, created_at)

-- Stock management
factory_stock (id, product_id FK, quantity, low_stock_threshold, updated_at)
outlets (id, name, location, phone, created_at)
outlet_stock (id, outlet_id FK, product_id FK, quantity, updated_at)

-- Transfers
stock_transfers (id, product_id FK, outlet_id FK, quantity,
                 transfer_date, note, status)

-- POS & Sales
sales (id, outlet_id FK, cashier_id FK, total_amount, 
       payment_method, sale_date)
sale_items (id, sale_id FK, product_id FK, quantity, 
            unit_price, subtotal)

-- HR
employees (id, name, nic, phone, email, position,
           department, salary, hire_date, status)
```

**Relationships:**
- `factory_stock` → `products` (1:1, each product has one factory stock entry)
- `outlet_stock` → `outlets` and `products` (many-to-many bridge)
- `stock_transfers` → `products`, `outlets` (many-to-one)
- `sales` → `outlets`, `users` (many-to-one)
- `sale_items` → `sales`, `products` (many-to-one)

---

## Modules Implementation Plan

### Module 1 — Authentication & User Management
- Spring Security login form at `/login`
- Two roles: `ADMIN` (full access), `CASHIER` (POS only)
- BCrypt password hashing
- Custom `UserDetailsService`
- Session-based auth with CSRF protection

### Module 2 — Product Management (Admin only)
- CRUD operations for products
- Fields: name, description, price, category
- Bootstrap table with search filter
- Linked to factory stock on creation

### Module 3 — Factory Stock Management (Admin)
- View all products and their factory stock levels
- Adjust stock quantity (add/subtract)
- Visual badge for low-stock items (threshold configurable)
- Stock movement log (simple audit)

### Module 4 — Outlet Management (Admin)
- Add/edit/delete outlets
- View outlet stock per outlet
- Outlet list with details card

### Module 5 — Stock Transfer (Admin / Factory Manager)
- Select product + outlet + quantity
- Auto deduct from factory stock
- Auto add to outlet stock
- Transfer status: PENDING → COMPLETED
- Transfer history table

### Module 6 — POS / Sales (Cashier + Admin)
- Product search by ID or name
- Add to cart (JavaScript cart, no reload)
- Display cart total dynamically
- Payment form (cash/card)
- On submit: save Sale + SaleItems, deduct outlet stock
- Printable bill page

### Module 7 — Employee Management (Admin)
- Full CRUD for employees
- Fields: name, NIC, phone, position, department, salary, hire date
- Bootstrap table with search
- Active/Inactive status toggle

### Module 8 — Reports (Admin)
- **Daily Sales Report**: Date picker → Sales total + breakdown
- **Stock Report**: Factory + outlet stock summary table
- **Transfer Report**: All transfers with filter by date/outlet
- All printable from browser (CSS print styles included)

### Module 9 — Dashboard (Admin)
- Summary cards: Total products, Today's sales, Total employees, Outlets
- Low stock alerts panel
- Recent sales table (last 10)
- Simple stat counters

---

## Security Configuration & Cashier Login Bug Fix

### Cashier Login 403 Forbidden Root Cause
1. **Redirect on Login**: The login flow was configured with `.defaultSuccessUrl("/dashboard", true)`. This always redirects both `ADMIN` and `CASHIER` to `/dashboard`.
2. **Dashboard Permissions**: `/dashboard` requires the `ADMIN` role. When a cashier logs in, they are redirected to `/dashboard` and get a `403 Forbidden` error.
3. **Access Denied Loop**: When authorization fails, Spring Security forwards the request to `/access-denied`. However, `/access-denied` was not exempted from security rules and fell under `.anyRequest().hasRole("ADMIN")`. This caused a recursive access denied loop (forwarding to `/access-denied` which also gets a 403, and so on), leading to a stack overflow / filter exception.

### Proposed Fixes
1. **Dynamic Login Redirect**: Modify `SecurityConfig.java` to use a custom success handler that checks the user's role:
   - `ROLE_ADMIN` -> `/dashboard`
   - `ROLE_CASHIER` -> `/pos`
2. **Access Denied Page Permitted**: Update `SecurityConfig.java` to explicitly permitAll for `/access-denied`.
3. **Root URL Redirection**: Update the `/` pattern in `SecurityConfig.java` to permitAll (or authenticated) and update `DashboardController.java` to dynamically redirect users based on their role:
   - `ROLE_CASHIER` -> `/pos`
   - `ROLE_ADMIN` -> `/dashboard`
4. **Sidebar Visibility**: Wrap the Dashboard link in `sidebar.html` with `sec:authorize="hasRole('ADMIN')"` so cashiers do not see a link to `/dashboard` that they cannot access.

| URL Pattern | Role Access |
|---|---|
| `/` | Public (dynamically redirects based on role) |
| `/login` | Public |
| `/access-denied` | Public |
| `/dashboard/**` | ADMIN |
| `/product/**` | ADMIN |
| `/stock/**` | ADMIN |
| `/outlet/**` | ADMIN |
| `/transfer/**` | ADMIN |
| `/employee/**` | ADMIN |
| `/report/**` | ADMIN |
| `/pos/**` | ADMIN, CASHIER |
| `/sales/**` | ADMIN, CASHIER |

---

## UI Design

- **Layout**: Sidebar navigation (Bootstrap offcanvas/fixed) + top navbar
- **Theme**: Light professional theme with Bootstrap 5
- **Dashboard**: Card-based stats + table
- **Forms**: Floating labels with validation feedback
- **Tables**: Striped, hoverable, with action buttons
- **POS**: Two-column layout — product search left, cart right
- **Responsive**: Mobile-friendly with Bootstrap grid

---

## Build Order (Step-by-Step)

1. `pom.xml` — Maven dependencies
2. `application.properties` — DB config, security config
3. All **Entity** classes with JPA annotations
4. All **Repository** interfaces
5. All **Service** classes (business logic)
6. `SecurityConfig.java` + `UserDetailsService`
7. All **Controllers**
8. **Thymeleaf templates** — layout base, then each module
9. **POS JavaScript** — cart logic
10. **Sample data** SQL script
11. **Report pages** with print CSS

---

## Open Questions

> [!IMPORTANT]
> **MySQL Credentials**: What are your local MySQL username and password? (default is `root` / `root` or `root` / empty). The `application.properties` will need these.

> [!IMPORTANT]
> **Database Name**: What should the database be named? Suggestion: `factory_management_db`

> [!NOTE]
> **Port**: Spring Boot will run on `http://localhost:8080` by default. Is this acceptable?

> [!NOTE]
> **PDF Reports**: Should I include PDF export using iText library, or are browser-printable HTML reports sufficient for the project?

---

## Verification Plan

### Automated
- Application starts without errors (`mvn spring-boot:run`)
- Login page loads at `http://localhost:8080/login`
- Sample data seeds correctly on startup

### Manual Testing
- Login with ADMIN and CASHIER roles
- Create a product → add factory stock → create outlet → transfer stock → complete a POS sale → view report
- Confirm all stock numbers update correctly end-to-end
