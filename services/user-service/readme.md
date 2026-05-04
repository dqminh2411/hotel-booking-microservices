# User Service

## Overview

**User Service** là Entity Service (Agnostic) chịu trách nhiệm quản lý thông tin người dùng và phân quyền trong hệ thống.

- **Business domain**: Quản lý tài khoản người dùng, vai trò (role) và phân quyền
- **Data ownership**: Sở hữu toàn bộ dữ liệu về `users`, `roles`, `user_roles`
- **Vai trò trong hệ thống**: Được gọi bởi `PlaceBookingService` (sync REST, client-side discovery qua Eureka) để xác minh user tồn tại và lấy thông tin (tên, email) trước khi khởi động Saga đặt phòng

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.13 |
| Database | PostgreSQL |
| Service Discovery | Spring Cloud Netflix Eureka Client |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check — trả về `{"status": "ok"}` |
| GET | `/users/{userId}` | Lấy thông tin user theo ID kèm danh sách role |

> Full API specification: [`docs/api-specs/user-service.yaml`](../../docs/api-specs/user-service.yaml)

## Database Schema

### `users` — Thông tin người dùng hệ thống

| Field | Datatype | Constraint | Description |
|-------|----------|------------|----------|
| `id` | varchar(255) | PRIMARY KEY | id người dùng (ví dụ: `US-001`) |
| `name` | text | NOT NULL | Tên người dùng |
| `email` | varchar(255) | NOT NULL, UNIQUE | Địa chỉ email đăng nhập |
| `phone` | varchar(20) | | Số điện thoại |
| `password` | varchar(255) | NOT NULL | Mật khẩu |
| `created_at` | timestamp | DEFAULT NOW() | Thời gian tạo tài khoản |

### `roles` — Danh sách vai trò trong hệ thống

| Field | Datatype | Constraint | Description                          |
|-------|----------|------------|--------------------------------------|
| `id` | varchar(255) | PRIMARY KEY | id của role (ví dụ: `RO-001`)        |
| `name` | varchar(255) | NOT NULL, UNIQUE | Tên role (ví dụ: `CUSTOMER`, `HOST`) |
| `description` | text | | Mô tả vai trò                        |

### `user_roles` — Bảng trung gian users ↔ roles (N-N)

| Field | Datatype | Constraint | Description |
|-------|----------|------------|-------------|
| `user_id` | varchar(255) | PK, FK → users.id | ID người dùng |
| `role_id` | varchar(255) | PK, FK → roles.id | ID vai trò |

> `(user_id, role_id)` là composite primary key.

## Running Locally

```bash
# Chạy toàn bộ hệ thống từ project root
docker compose up --build

# Chỉ chạy service này (và dependencies)
docker compose up user-service user-db eureka-server --build
```

## Project Structure

```
user-service/
├── Dockerfile
├── readme.md
└── src/
    └── main/
        ├── java/com/hotel/userservice/
        │   ├── controller/     # REST controllers
        │   ├── service/        # Business logic
        │   ├── repository/     # JPA repositories
        │   ├── entity/         # JPA entities
        │   └── dto/            # Request/Response DTOs
        └── resources/
            └── application.properties
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Port nội bộ của service | `5000` |
| `DB_HOST` | Hostname của PostgreSQL | `user-db` |
| `DB_PORT` | Port của PostgreSQL | `5432` |
| `DB_NAME` | Tên database | `user_db` |
| `DB_USER` | Username PostgreSQL | `postgres` |
| `DB_PASSWORD` | Password PostgreSQL | *(required)* |
| `EUREKA_ENABLED` | Bật/tắt Eureka client | `false` |
| `EUREKA_SERVER_URL` | URL Eureka Server | `http://eureka-server:8761/eureka` |

## Running Locally

```bash
# Build và chạy user-service + user-db
docker compose up --build user-db user-service
```

## Quick Verification

```bash
curl http://localhost:5002/health
curl http://localhost:5002/users/US-001
```

Response mẫu:

```json
{
  "userId": "US-001",
  "name": "Nguyen Van An",
  "email": "an.nguyen@email.com",
  "phone": "0901234561",
  "createdAt": "2024-01-15T08:30:00Z",
  "roles": ["CUSTOMER"]
}
```
