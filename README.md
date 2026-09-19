# ShopVerse — Full-Stack E-Commerce Application

A production-quality e-commerce platform built with React + TypeScript (frontend) and Spring Boot 3 (backend).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Prerequisites](#4-prerequisites)
5. [Project Structure](#5-project-structure)
6. [Database Setup](#6-database-setup)
7. [Backend Setup & Configuration](#7-backend-setup--configuration)
8. [Frontend Setup](#8-frontend-setup)
9. [Running the Application](#9-running-the-application)
10. [Default Accounts](#10-default-accounts)
11. [API Reference](#11-api-reference)
12. [Authentication Flow](#12-authentication-flow)
13. [Admin Panel](#13-admin-panel)
14. [Security Design](#14-security-design)
15. [Testing](#15-testing)
16. [Environment Variables](#16-environment-variables)

---

## 1. Overview

ShopVerse is a fully-featured online store with two distinct user roles:

- **Customer (ROLE_USER)**: Browse products, manage cart, place and track orders, update profile.
- **Admin (ROLE_ADMIN)**: Manage products, categories, users, and orders via a dedicated dashboard.

Key design decisions:
- All order totals are calculated server-side — frontend prices are never trusted.
- Passwords are always BCrypt-hashed. Password hashes are never exposed in API responses.
- Payment is a demo-only flow. No real card data is collected or stored.
- Stock is decremented atomically at checkout, not at "add to cart" time.
- Deleted products are soft-deleted (set `active = false`) so historical order items retain their data.

---

## 2. Architecture

```
┌──────────────────────────────┐     HTTP/JSON      ┌──────────────────────────────┐
│   React + TypeScript (Vite)  │  ─────────────────► │  Spring Boot 3 REST API      │
│   Port: 5173 (dev)           │  ◄─────────────────  │  Port: 8080                  │
│   Tailwind CSS 3             │                     │  Spring Security 6 + JWT     │
│   React Router v6            │                     │  Spring Data JPA             │
│   React Hook Form + Zod      │                     │  Flyway migrations           │
└──────────────────────────────┘                     └──────────────┬───────────────┘
                                                                    │
                                                      ┌─────────────▼───────────────┐
                                                      │  PostgreSQL 15              │
                                                      │  (or H2 for local/tests)    │
                                                      └─────────────────────────────┘
```

The Vite dev server proxies all `/api/*` requests to `http://localhost:8080`, so no CORS issues in development.

---

## 3. Technology Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Runtime |
| Spring Boot | 3.2.3 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | ORM / database layer |
| Hibernate | 6.x | JPA implementation |
| JJWT | 0.12.3 | JWT generation and validation |
| BCrypt | — | Password hashing |
| Flyway | 9.x | Database migrations |
| PostgreSQL | 15 | Production database |
| H2 | — | Development & test database |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI / API docs |
| Lombok | — | Boilerplate reduction |
| JUnit 5 + Mockito | — | Unit & integration tests |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18.2 | UI framework |
| TypeScript | 5.3 | Type safety |
| Vite | 5.1 | Build tool & dev server |
| Tailwind CSS | 3.4 | Utility-first styling |
| React Router | 6.22 | Client-side routing |
| Axios | 1.6.7 | HTTP client with interceptors |
| React Hook Form | 7.50 | Form state management |
| Zod | 3.22 | Schema validation |
| Lucide React | 0.344 | Icons |
| react-hot-toast | 2.4 | Toast notifications |
| Vitest | 1.3 | Unit test runner |

---

## 4. Prerequisites

| Tool | Minimum Version | Check |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.8 | `mvn -version` |
| Node.js | 18 | `node -version` |
| npm | 9 | `npm -version` |
| Docker (optional) | 24 | `docker -version` |
| PostgreSQL (optional) | 15 | `psql --version` |

> **No PostgreSQL?** Use the `local` Spring profile which runs with H2 in-memory. See [Running the Application](#9-running-the-application).

---

## 5. Project Structure

```
Ecommerce-application/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/shopverse/
│       │   │   ├── ShopVerseApplication.java
│       │   │   ├── config/          # Security, OpenAPI, DataInitializer
│       │   │   ├── controller/      # REST controllers
│       │   │   ├── dto/
│       │   │   │   ├── request/     # Incoming request bodies
│       │   │   │   └── response/    # Outgoing response bodies
│       │   │   ├── entity/          # JPA entities
│       │   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│       │   │   ├── mapper/          # Entity → DTO mappers
│       │   │   ├── repository/      # Spring Data JPA repositories
│       │   │   ├── security/        # JWT provider, filter, entry point
│       │   │   └── service/         # Business logic
│       │   └── resources/
│       │       ├── application.yml          # Production config (PostgreSQL)
│       │       ├── application-local.yml    # Dev config (H2 in-memory)
│       │       ├── application-test.yml     # Test config (H2 in-memory)
│       │       └── db/migration/
│       │           ├── V1__create_tables.sql
│       │           └── V2__seed_data.sql
│       └── test/
│           └── java/com/shopverse/
│               ├── service/         # Unit tests (Mockito)
│               └── controller/      # Integration tests (MockMvc)
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── src/
│       ├── api/             # Axios API client modules
│       ├── components/
│       │   ├── layout/      # Navbar, Footer, AdminLayout, AdminSidebar
│       │   └── ui/          # Button, Input, Modal, Badge, Pagination, etc.
│       ├── context/         # AuthContext, CartContext
│       ├── pages/
│       │   ├── admin/       # Admin dashboard, products, categories, users, orders
│       │   └── *.tsx        # Customer pages
│       ├── router/          # AppRouter, PrivateRoute, AdminRoute
│       └── types/           # Shared TypeScript interfaces
├── docker-compose.yml
└── README.md
```

---

## 6. Database Setup

### Option A — Docker (recommended)

```bash
# From project root
docker-compose up -d

# Verify
docker-compose ps
# shopverse-postgres should be "Up"
```

This starts PostgreSQL 15 with:
- Database: `shopverse`
- User: `shopverse_user`
- Password: `shopverse_pass`
- Port: `5432`

Flyway migrations run automatically on first startup and seed 6 categories + 15 products.

### Option B — Local PostgreSQL

```sql
-- Run as postgres superuser
CREATE DATABASE shopverse;
CREATE USER shopverse_user WITH PASSWORD 'shopverse_pass';
GRANT ALL PRIVILEGES ON DATABASE shopverse TO shopverse_user;
```

### Option C — H2 In-Memory (no setup required)

Use the `local` Spring profile. Data is ephemeral and reset on every restart. No Flyway. Schema auto-created by Hibernate.

---

## 7. Backend Setup & Configuration

```bash
cd backend
mvn clean compile   # Verify compilation
```

### Configuration files

**`application.yml`** — used in production / when no profile is active:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/shopverse}
    username: ${SPRING_DATASOURCE_USERNAME:shopverse_user}
    password: ${SPRING_DATASOURCE_PASSWORD:shopverse_pass}
app:
  jwt:
    secret: ${APP_JWT_SECRET:<256-bit-base64-secret>}
    expiration-ms: ${APP_JWT_EXPIRATION_MS:86400000}   # 24 hours
  admin:
    email: ${APP_ADMIN_EMAIL:admin@shopverse.com}
    password: ${APP_ADMIN_PASSWORD:Admin@123456}
```

**`application-local.yml`** — H2 in-memory, H2 console at `/h2-console`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:shopversedb
  h2:
    console.enabled: true
  flyway.enabled: false
  jpa.hibernate.ddl-auto: create-drop
```

Override any value with an environment variable (the `${}` syntax with defaults above). For production, always set `APP_JWT_SECRET` to a securely generated 256-bit Base64 string.

---

## 8. Frontend Setup

```bash
cd frontend
npm install
```

Vite proxies `/api` to `http://localhost:8080` automatically during development — no manual CORS config needed.

---

## 9. Running the Application

### Development (H2 in-memory — no database setup)

**Terminal 1 — Backend:**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Terminal 2 — Frontend:**
```bash
cd frontend
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

H2 console available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
JDBC URL: `jdbc:h2:mem:shopversedb`

### With PostgreSQL (Docker)

```bash
# Start database
docker-compose up -d

# Start backend (no profile = PostgreSQL + Flyway)
cd backend && mvn spring-boot:run

# Start frontend
cd frontend && npm run dev
```

### Production build

```bash
# Build frontend
cd frontend && npm run build
# Artifacts in frontend/dist/

# Build backend fat jar
cd backend && mvn clean package -DskipTests
java -jar target/shopverse-*.jar
```

---

## 10. Default Accounts

These accounts are created automatically on first startup by `DataInitializer`:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@shopverse.com` | `Admin@123456` |

Register any number of customer accounts through the UI at `/register`.

> **Important**: Change the default admin password in production via the `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` environment variables.

---

## 11. API Reference

Swagger UI is available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Summary of endpoints

#### Authentication — `/api/auth`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new customer |
| POST | `/api/auth/login` | Public | Login, returns JWT |

#### Products — `/api/products`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/products` | Public | List products (paginated, filterable) |
| GET | `/api/products/{id}` | Public | Get single active product |
| POST | `/api/products` | Admin | Create product |
| PUT | `/api/products/{id}` | Admin | Update product |
| DELETE | `/api/products/{id}` | Admin | Soft-delete product |

Query params for GET `/api/products`: `page`, `size`, `sort` (`price_asc`, `price_desc`, `name_asc`, `name_desc`, `createdAt_desc`), `search`, `categoryId`, `minPrice`, `maxPrice`.

#### Categories — `/api/categories`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/categories` | Public | List all categories |
| GET | `/api/categories/{id}` | Public | Get single category |
| POST | `/api/categories` | Admin | Create category |
| PUT | `/api/categories/{id}` | Admin | Update category |
| DELETE | `/api/categories/{id}` | Admin | Delete category |

#### Cart — `/api/cart`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/cart` | User | Get current user's cart |
| POST | `/api/cart/items` | User | Add item to cart |
| PUT | `/api/cart/items/{itemId}` | User | Update item quantity |
| DELETE | `/api/cart/items/{itemId}` | User | Remove item |
| DELETE | `/api/cart` | User | Clear cart |

#### Orders — `/api/orders`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/orders/checkout` | User | Place order (clears cart) |
| GET | `/api/orders` | User | List own orders (paginated) |
| GET | `/api/orders/{id}` | User | Get own order detail |
| POST | `/api/orders/{id}/cancel` | User | Cancel PENDING/CONFIRMED order |

#### User Profile — `/api/users`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users/me` | User | Get own profile |
| PUT | `/api/users/me` | User | Update profile |
| POST | `/api/users/me/change-password` | User | Change password |

#### Admin — `/api/admin`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Admin | Dashboard statistics |
| GET | `/api/admin/users` | Admin | List all users (paginated) |
| GET | `/api/admin/users/{id}` | Admin | Get user by ID |
| POST | `/api/admin/users/{id}/toggle-status` | Admin | Enable/disable user |
| GET | `/api/admin/orders` | Admin | List all orders (filterable by status) |
| PUT | `/api/admin/orders/{id}/status` | Admin | Update order status |
| GET | `/api/admin/products` | Admin | List all products incl. inactive |

---

## 12. Authentication Flow

```
1. POST /api/auth/login  →  { token: "eyJ...", email, firstName, lastName, role }
2. Store token in localStorage
3. Every subsequent request: Authorization: Bearer <token>
4. Token expires after 24 hours (configurable)
5. On 401 response: Axios interceptor redirects to /login
```

JWT payload contains: `sub` (email), `role` (ROLE_USER or ROLE_ADMIN), `iat`, `exp`.

Method-level security (`@PreAuthorize("hasRole('ADMIN')")`) prevents role escalation even if a token is somehow obtained by the wrong party.

---

## 13. Admin Panel

Access at `/admin` after logging in with an admin account.

| Page | Path | Features |
|---|---|---|
| Dashboard | `/admin` | Total users, products, orders, pending orders, revenue |
| Products | `/admin/products` | Table with search, create/edit modal, soft-delete |
| Categories | `/admin/categories` | Full CRUD with confirmation dialogs |
| Users | `/admin/users` | Search, enable/disable (cannot disable admins) |
| Orders | `/admin/orders` | Filter by status, inline status update, order detail modal |

Admin routes are protected by `AdminRoute` — regular users who navigate to `/admin/*` are redirected to `/`.

---

## 14. Security Design

| Concern | Implementation |
|---|---|
| Passwords | BCrypt hash, never stored or returned in plaintext |
| JWT secret | Loaded from environment variable; not hardcoded in production |
| Price integrity | Backend recalculates order total from DB prices; frontend total is ignored |
| Admin authorization | Spring Security method-level + URL-pattern guards; both layers required |
| Payment | Demo-only mock flow; no card data collected or stored |
| CSRF | Disabled — stateless JWT API does not use session cookies |
| CORS | Restricted to `localhost:5173` / `localhost:3000` in dev config |
| SQL injection | Prevented by parameterized JPA queries; no string-concatenated SQL |
| Soft deletes | Products deactivated, not removed; preserves order item history |

---

## 15. Testing

### Run backend tests

```bash
cd backend
mvn test
```

Tests use the `test` Spring profile (H2 in-memory, Flyway disabled). No external database required.

**Unit tests** (Mockito):
- `AuthServiceTest` — register, login, duplicate email, bad credentials
- `ProductServiceTest` — CRUD, soft delete, SKU uniqueness
- `CategoryServiceTest` — CRUD, duplicate name validation

**Integration tests** (MockMvc + full Spring context):
- `AuthControllerIntegrationTest` — full register/login cycle, validation errors
- `ProductControllerIntegrationTest` — public list, admin create, 401 for unauthenticated

### Run frontend tests

```bash
cd frontend
npm test
```

---

## 16. Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/shopverse` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `shopverse_user` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `shopverse_pass` | DB password |
| `APP_JWT_SECRET` | (hardcoded dev value — **change in prod**) | 256-bit Base64 JWT signing key |
| `APP_JWT_EXPIRATION_MS` | `86400000` | JWT lifetime in ms (default: 24 h) |
| `APP_ADMIN_EMAIL` | `admin@shopverse.com` | Default admin email |
| `APP_ADMIN_PASSWORD` | `Admin@123456` | Default admin password |

> **Production checklist**:
> - Set `APP_JWT_SECRET` to a randomly generated 256-bit Base64 string.
> - Set `APP_ADMIN_PASSWORD` to a strong unique password.
> - Set `SPRING_DATASOURCE_PASSWORD` to a strong unique password.
> - Restrict `CORS` origins in `SecurityConfig` to your actual frontend domain.
> - Run behind HTTPS.

---

*ShopVerse — built for demonstration and educational purposes.*
