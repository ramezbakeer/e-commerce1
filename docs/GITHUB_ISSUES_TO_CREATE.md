# GitHub Issues to Complete e-commerce Project

Use this file to create issues on GitHub (https://github.com/ramezbakeer/e-commerce/issues/new). Copy each **Title** and **Body** below into a new issue.

---

## Issue 1: Domain – Product and Category entities

**Title:** `[Domain] Add Product and Category entities with JPA`

**Body:**
```markdown
## Summary
Add JPA entities for the core catalog: `Product` and `Category`.

## Acceptance criteria
- [ ] `Category` entity: id, name, slug, description, parent (self-reference for hierarchy), timestamps
- [ ] `Product` entity: id, name, slug, description, price, sku, stock quantity, category (many-to-one), image URL(s), active flag, timestamps
- [ ] JPA repositories: `CategoryRepository`, `ProductRepository`
- [ ] Use `@Entity`, proper indexes, and validation annotations where applicable
```

**Labels:** `domain`, `enhancement`

---

## Issue 2: Domain – User/Customer entity

**Title:** `[Domain] Add User/Customer entity and authentication fields`

**Body:**
```markdown
## Summary
Add `User` (or `Customer`) entity for accounts and future auth.

## Acceptance criteria
- [ ] Entity: id, email (unique), password hash, name, role (e.g. CUSTOMER, ADMIN), enabled, timestamps
- [ ] `UserRepository` with `Optional<User> findByEmail(String email)`
- [ ] Do not store plain passwords; document that encoding will be used with Spring Security
```

**Labels:** `domain`, `enhancement`

---

## Issue 3: Domain – Cart and CartItem entities

**Title:** `[Domain] Add Cart and CartItem entities`

**Body:**
```markdown
## Summary
Add entities for shopping cart: `Cart` and `CartItem`.

## Acceptance criteria
- [ ] `Cart`: id, user (optional for anonymous), session id (for guest cart), created/updated timestamps
- [ ] `CartItem`: id, cart, product, quantity, added at
- [ ] Repositories: `CartRepository`, `CartItemRepository`
- [ ] Support both authenticated users and anonymous (session-based) carts
```

**Labels:** `domain`, `enhancement`

---

## Issue 4: Domain – Order and OrderItem entities

**Title:** `[Domain] Add Order and OrderItem entities`

**Body:**
```markdown
## Summary
Add entities for orders: `Order` and `OrderItem`.

## Acceptance criteria
- [ ] `Order`: id, user, status (e.g. PENDING, PAID, SHIPPED, DELIVERED, CANCELLED), total amount, shipping address (or embed), timestamps
- [ ] `OrderItem`: id, order, product snapshot (name, price at order time), quantity, subtotal
- [ ] Repositories: `OrderRepository`, `OrderItemRepository`
- [ ] Consider an `OrderStatus` enum
```

**Labels:** `domain`, `enhancement`

---

## Issue 5: REST API – Product and Category endpoints

**Title:** `[API] Product and Category REST endpoints`

**Body:**
```markdown
## Summary
Expose REST API for products and categories.

## Acceptance criteria
- [ ] **Categories:** GET list (tree or flat), GET by id/slug
- [ ] **Products:** GET list with pagination and optional filters (category, search), GET by id/slug
- [ ] DTOs for request/response (no entity leakage)
- [ ] Use `@RestController`, `@GetMapping`/`@PostMapping` etc., and validation
- [ ] Consistent JSON and HTTP status codes (404 when not found)
```

**Labels:** `api`, `enhancement`

---

## Issue 6: REST API – Cart endpoints

**Title:** `[API] Cart REST endpoints (add, update, remove items)`

**Body:**
```markdown
## Summary
REST API to manage cart: add item, update quantity, remove item, get cart.

## Acceptance criteria
- [ ] GET /cart – return current cart (by session or user)
- [ ] POST /cart/items – add product to cart (product id, quantity)
- [ ] PATCH/PUT /cart/items/{id} – update quantity
- [ ] DELETE /cart/items/{id} – remove line
- [ ] DTOs and validation (e.g. quantity > 0, product exists)
```

**Labels:** `api`, `enhancement`

---

## Issue 7: REST API – Checkout and orders

**Title:** `[API] Checkout and order endpoints`

**Body:**
```markdown
## Summary
Allow placing an order from the cart and viewing order history.

## Acceptance criteria
- [ ] POST /orders/checkout – create order from current cart (with shipping/contact info if needed)
- [ ] GET /orders – list orders for current user (paginated)
- [ ] GET /orders/{id} – order detail (with items)
- [ ] Clear or merge cart after successful checkout
- [ ] Return 201 and order representation on success
```

**Labels:** `api`, `enhancement`

---

## Issue 8: Security – Enable and configure Spring Security

**Title:** `[Security] Enable Spring Security and implement auth`

**Body:**
```markdown
## Summary
Uncomment and configure Spring Security. Implement authentication (and optionally JWT for stateless API).

## Acceptance criteria
- [ ] Enable `spring-boot-starter-security` in pom.xml
- [ ] Configure security: public access for product/category listing and detail; protected for cart and orders (authenticated or session)
- [ ] Implement user registration (POST /auth/register) and login (e.g. POST /auth/login returning token or session)
- [ ] Password encoding (e.g. BCrypt)
- [ ] Optional: JWT for API auth; or session-based auth for web
```

**Labels:** `security`, `enhancement`

---

## Issue 9: API – Global exception handling and validation

**Title:** `[API] Global exception handler and consistent error responses`

**Body:**
```markdown
## Summary
Centralized error handling and consistent JSON error format.

## Acceptance criteria
- [ ] `@ControllerAdvice` (or `@RestControllerAdvice`) for handling exceptions
- [ ] Map: EntityNotFoundException → 404, validation errors → 400, auth → 401/403, server errors → 500
- [ ] Consistent error DTO (e.g. message, code, field errors for validation)
- [ ] Use `@Valid` and Bean Validation on request DTOs where applicable
```

**Labels:** `api`, `enhancement`

---

## Issue 10: API – Pagination and filtering

**Title:** `[API] Pagination and filtering for product list`

**Body:**
```markdown
## Summary
Support pagination and filtering on product list endpoint.

## Acceptance criteria
- [ ] Query params: page, size, sort (e.g. price, name)
- [ ] Optional filters: categoryId/categorySlug, minPrice, maxPrice, search (name/description)
- [ ] Response includes page metadata (total elements, total pages, current page)
- [ ] Use Spring Data `Pageable` and `Page<T>`
```

**Labels:** `api`, `enhancement`

---

## Issue 11: Documentation – OpenAPI/Swagger

**Title:** `[Docs] Add OpenAPI (Swagger) documentation`

**Body:**
```markdown
## Summary
Add OpenAPI 3 / Swagger UI for the REST API.

## Acceptance criteria
- [ ] Add springdoc-openapi (or springfox) dependency
- [ ] Swagger UI available at e.g. /swagger-ui.html
- [ ] API description and main endpoints documented with annotations or separate spec
- [ ] Document request/response schemas for main endpoints
```

**Labels:** `documentation`, `enhancement`

---

## Issue 12: Documentation – README and setup

**Title:** `[Docs] README with setup and run instructions`

**Body:**
```markdown
## Summary
Improve README so anyone can clone and run the project.

## Acceptance criteria
- [ ] Project description and tech stack (Spring Boot, Java 17, MySQL, etc.)
- [ ] Prerequisites (JDK 17, Maven, MySQL)
- [ ] How to create DB and set `application.properties` (or env vars)
- [ ] How to run: `./mvnw spring-boot:run`
- [ ] Optional: link to API docs (Swagger) and main endpoints
```

**Labels:** `documentation`, `enhancement`

---

## Issue 13: Testing – Service and repository tests

**Title:** `[Tests] Unit and repository tests for services and repositories`

**Body:**
```markdown
## Summary
Add tests for business logic and data access.

## Acceptance criteria
- [ ] Unit tests for service layer (e.g. cart service, order service) with mocks
- [ ] Repository tests (e.g. `@DataJpaTest`) for custom queries
- [ ] Tests pass with `./mvnw test`
- [ ] Use JUnit 5 and Mockito; consider Testcontainers for DB if needed
```

**Labels:** `testing`, `enhancement`

---

## Issue 14: Testing – API integration tests

**Title:** `[Tests] REST API integration tests`

**Body:**
```markdown
## Summary
Integration tests for main REST endpoints.

## Acceptance criteria
- [ ] `@SpringBootTest` with MockMvc or TestRestTemplate
- [ ] Tests for: get products (list, by id), get categories, cart operations, checkout (happy path)
- [ ] Use in-memory H2 or Testcontainers for DB so CI can run tests
- [ ] Document how to run tests
```

**Labels:** `testing`, `enhancement`

---

## Issue 15: DevOps – Docker and docker-compose

**Title:** `[DevOps] Docker and docker-compose for local run`

**Body:**
```markdown
## Summary
Run the app and MySQL via Docker for local development.

## Acceptance criteria
- [ ] Dockerfile for the Spring Boot app (multi-stage build)
- [ ] docker-compose.yml: app service + MySQL (or MariaDB)
- [ ] Env/config so app connects to DB in Docker network
- [ ] README section: how to run with `docker-compose up`
```

**Labels:** `devops`, `enhancement`

---

## Issue 16: Configuration – Environment profiles

**Title:** `[Config] Environment profiles (dev, prod)`

**Body:**
```markdown
## Summary
Support different configurations per environment.

## Acceptance criteria
- [ ] application-dev.properties: local DB, optional H2 for tests, relaxed security if needed
- [ ] application-prod.properties: production DB URL from env, ddl-auto=validate or none
- [ ] Document use of `spring.profiles.active=dev|prod` and env-specific settings
```

**Labels:** `configuration`, `enhancement`

---

## Quick reference – Suggested order

1. **Domain:** 1 → 2 → 3 → 4  
2. **API:** 5 → 6 → 7, then 9 and 10  
3. **Security:** 8  
4. **Docs:** 11, 12  
5. **Tests:** 13, 14  
6. **DevOps/Config:** 15, 16  

Create issues at: **https://github.com/ramezbakeer/e-commerce/issues/new**
