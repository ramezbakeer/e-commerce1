# E-Commerce Backend

A production-ready REST API built with Spring Boot, featuring JWT authentication, a full product catalog, shopping cart (guest + authenticated), and order management.

---

## Tech stack

- **Java 17**
- **Spring Boot 4.x** (Web MVC, Data JPA, Security, Validation)
- **MySQL 8**
- **JJWT 0.12** for JWT auth
- **Lombok**
- **springdoc-openapi** for Swagger UI

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17+     |
| Maven | 3.9+ (or use the included `./mvnw`) |
| MySQL | 8.0+   |

---

## Database setup

```sql
CREATE DATABASE `e-commerce`;
```

---

## Configuration

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/e-commerce?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

app.jwt.secret=YOUR_SECRET_KEY_MIN_32_CHARS
app.jwt.expiration-ms=86400000
```

> `spring.jpa.hibernate.ddl-auto=update` will create tables automatically on first run.

---

## Running the app

```bash
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**.

---

## API documentation

Swagger UI is available once the app is running:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/api-docs
```

---

## Main endpoints

### Auth
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/auth/register` | Register a new account | Public |
| POST | `/api/v1/auth/login` | Login and receive a JWT | Public |

### Products
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/products` | List products (paginated + filtered) | Public |
| GET | `/api/v1/products/{id}` | Get product by ID | Public |
| GET | `/api/v1/products/slug/{slug}` | Get product by slug | Public |
| POST | `/api/v1/products` | Create product | Admin |
| PATCH | `/api/v1/products/{id}` | Update product | Admin |
| DELETE | `/api/v1/products/{id}` | Delete product | Admin |

**Product list query params:**

| Param | Type | Description |
|-------|------|-------------|
| `page` | int | Page index (default: 0) |
| `size` | int | Page size (default: 20, max: 100) |
| `sort` | string | e.g. `price,asc` or `name,desc` (default: `createdAt,desc`) |
| `categoryId` | Long | Filter by category ID |
| `categorySlug` | string | Filter by category slug |
| `minPrice` | decimal | Minimum price filter |
| `maxPrice` | decimal | Maximum price filter |
| `search` | string | Search in name and description |

### Categories
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/categories` | Category tree (add `?flat=true` for flat list) | Public |
| GET | `/api/v1/categories/{id}` | Get by ID | Public |
| GET | `/api/v1/categories/slug/{slug}` | Get by slug | Public |
| POST | `/api/v1/categories` | Create category | Admin |
| DELETE | `/api/v1/categories/{id}` | Delete category | Admin |

### Cart
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/cart` | Get cart (guests use `X-Session-Id` header) | Public |
| POST | `/api/v1/cart/items` | Add item | Bearer token |
| PATCH | `/api/v1/cart/items/{id}` | Update quantity | Bearer token |
| DELETE | `/api/v1/cart/items/{id}` | Remove item | Bearer token |
| DELETE | `/api/v1/cart` | Clear cart | Bearer token |

### Orders
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/orders/checkout` | Place order from cart | Bearer token |
| GET | `/api/v1/orders` | Order history (paginated) | Bearer token |
| GET | `/api/v1/orders/{id}` | Order detail | Bearer token |

---

## Authentication

Protected endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Obtain a token via `POST /api/v1/auth/login`.

---

## Error responses

All errors follow a consistent envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: '99'",
  "timestamp": "2025-03-16T12:00:00",
  "fieldErrors": null
}
```

`fieldErrors` is populated for `400` validation failures, with a map of `field → message`.

---

## Project structure

```
src/main/java/com/mawgod/e_commerce/
├── config/          # OpenAPI config
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Inbound request records
│   └── response/    # Outbound response records
├── entity/          # JPA entities
├── exception/       # Custom exceptions + global handler
├── mappers/         # Entity → DTO mappers
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, config, utils
└── service/         # Business logic
```
