# Hotel Service

## Overview

**Hotel Service** là Entity Service (Agnostic) chịu trách nhiệm quản lý toàn bộ thông tin khách sạn và loại phòng.

- **Business domain**: Quản lý danh mục khách sạn, loại phòng và tính toán availability
- **Data ownership**: Sở hữu toàn bộ dữ liệu về `hotels`, `room_types`
- **Vai trò trong hệ thống**:
  - Được client gọi trực tiếp qua Gateway (sync REST) để tìm kiếm khách sạn, xem chi tiết, xem danh sách loại phòng
  - Khi tính availability, hotel-service gọi nội bộ đến `booking-service` qua OpenFeign (`@FeignClient`) và Eureka client-side discovery để lấy số booking đang active, sau đó tính `availableRooms = quantity - activeBookingCount`
  - Được place-booking-service gọi để check availability trước khi khởi động Saga đặt phòng
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
| GET | `/hotels` | Tìm kiếm danh sách khách sạn theo tên, địa chỉ |
| GET | `/hotels/{hotelId}` | Lấy thông tin chi tiết khách sạn |
| GET | `/hotels/{hotelId}/room-types` | Lấy danh sách loại phòng (kèm availability nếu có checkin/checkout) |
| GET | `/hotels/{hotelId}/room-types/{roomTypeId}` | Lấy chi tiết một loại phòng |

> Full API specification: [`docs/api-specs/hotel-service.yaml`](../../docs/api-specs/hotel-service.yaml)

## Database Schema

### `hotels` — Thông tin khách sạn

| Field | Datatype | Constraint | Description |
|-------|----------|------------|-------------|
| `id` | varchar(255) | PRIMARY KEY | UUID có prefix (ví dụ: `HT-abc123`) |
| `name` | text | NOT NULL | Tên khách sạn |
| `description` | text | | Mô tả khách sạn |
| `host_id` | varchar(255) | NOT NULL | ID chủ khách sạn (FK → users.id) |
| `address` | text | NOT NULL | Địa chỉ đầy đủ |
| `image_url` | text | | URL ảnh đại diện |
| `created_at` | timestamp | DEFAULT NOW() | Thời gian tạo |

### `room_types` — Loại phòng của khách sạn

| Field | Datatype | Constraint | Description |
|-------|----------|------------|-------------|
| `id` | varchar(255) | PRIMARY KEY | UUID có prefix (ví dụ: `RT-abc123`) |
| `hotel_id` | varchar(255) | NOT NULL, FK → hotels.id | ID khách sạn |
| `name` | text | NOT NULL | Tên loại phòng (ví dụ: Deluxe Room) |
| `description` | text | | Mô tả loại phòng |
| `max_guests` | integer | NOT NULL | Số khách tối đa |
| `bed_counts` | integer | NOT NULL | Số giường |
| `base_price_per_night` | numeric | NOT NULL | Giá niêm yết mỗi đêm (VND) |
| `quantity` | integer | NOT NULL | Tổng số phòng loại này trong khách sạn |
| `area` | numeric | | Diện tích phòng (m²) |
| `image_url` | text | | URL ảnh đại diện |
| `created_at` | timestamp | DEFAULT NOW() | Thời gian tạo |

> **Availability logic:** `availableRooms = quantity - activeBookingCount`
> trong đó `activeBookingCount` được lấy từ booking-service qua `GET /bookings/count?roomTypeId=&checkin=&checkout=`

## Running Locally

```bash
# Chạy toàn bộ hệ thống từ project root
docker compose up --build

# Chỉ chạy hotel-service và database của nó
docker compose up hotel-service hotel-db --build
```

## Setup (Hotel Service only)

1. Cập nhật biến môi trường trong file `.env`.
2. Từ root project, chạy hotel service và database:

```bash
docker compose up hotel-db hotel-service --build
```

3. Verify endpoints:

```bash
curl http://localhost:5003/health
curl "http://localhost:5003/hotels?page=0&size=10"
curl http://localhost:5003/hotels/HT-001
curl "http://localhost:5003/hotels/HT-001/room-types?checkin=2026-06-01&checkout=2026-06-03"
curl "http://localhost:5003/hotels/HT-001/room-types/RT-001?checkin=2026-06-01&checkout=2026-06-03"
```

## Project Structure

```
hotel-service/
├── Dockerfile
├── readme.md
└── src/
    └── main/
        ├── java/com/hotelbooking/hotelservice/
        │   ├── controller/     # REST controllers
        │   ├── service/        # Business logic + availability calculation
        │   ├── repository/     # JPA repositories
        │   ├── entity/         # JPA entities (Hotel, RoomType)
        │   ├── dto/            # Request/Response DTOs
        │   └── client/         # OpenFeign client gọi booking-service qua Eureka
        └── resources/
            └── application.properties
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Port của service | `5000` |
| `DB_HOST` | Hostname của PostgreSQL | `hotel-db` |
| `DB_PORT` | Port của PostgreSQL (inside docker network) | `5432` |
| `DB_NAME` | Tên database | `hotel_db` |
| `DB_USER` | Username PostgreSQL | `postgres` |
| `DB_PASSWORD` | Password PostgreSQL | *(required)* |
| `EUREKA_ENABLED` | Bật/tắt Eureka client registration/discovery | `true` |
| `EUREKA_SERVER_URL` | URL Eureka Server | `http://eureka-server:8761/eureka` |
| `BOOKING_SERVICE_CONNECT_TIMEOUT_MS` | Feign connect timeout khi gọi booking-service | `2000` |
| `BOOKING_SERVICE_READ_TIMEOUT_MS` | Feign read timeout khi gọi booking-service | `5000` |

## Implementation Notes

- API được implement theo OpenAPI tại `docs/api-specs/hotel-service.yaml`.
- Các endpoint đã có:
  - `GET /health`
  - `GET /hotels`
  - `GET /hotels/{hotelId}`
  - `GET /hotels/{hotelId}/room-types`
  - `GET /hotels/{hotelId}/room-types/{roomTypeId}`
- Validation:
  - `page >= 0`, `1 <= size <= 100`
  - `hotelId`, `roomTypeId` chỉ cho phép ký tự `[A-Za-z0-9-]`
  - `checkin/checkout` phải truyền cùng nhau, `checkout > checkin`, `checkin` không ở quá khứ
- Error handling dùng `@RestControllerAdvice` với mã lỗi: `HOTEL_NOT_FOUND`, `ROOM_TYPE_NOT_FOUND`, `INVALID_DATE_RANGE`, `INVALID_REQUEST`.

