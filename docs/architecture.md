# System Architecture

> This document is completed **after** [Analysis and Design](analysis-and-design.md).
> Based on the Service Candidates and Non-Functional Requirements identified there, select appropriate architecture patterns and design the deployment architecture.

**References:**
1. *Service-Oriented Architecture: Analysis and Design for Services and Microservices* — Thomas Erl (2nd Edition)
2. *Microservices Patterns: With Examples in Java* — Chris Richardson
3. *Bài tập — Phát triển phần mềm hướng dịch vụ* — Hung Dang (available in Vietnamese)

---

## 1. Pattern Selection

| Pattern | Selected? | Business/Technical Justification |
|---------|-----------|----------------------------------|
| API Gateway | ✅ | Single entry point cho mọi request từ client. Xử lý JWT validation tập trung, routing đến đúng service, che giấu internal topology. Kết hợp với Eureka để server-side discovery. |
| Database per Service | ✅ | Mỗi service sở hữu database riêng, đảm bảo loose coupling và độc lập schema. Không service nào trực tiếp đọc DB của service khác — phải đi qua API hoặc Kafka event. |
| Shared Database | ❌ | Vi phạm service autonomy. Thay đổi schema của một service sẽ ảnh hưởng toàn bộ hệ thống — mất lợi thế deploy độc lập. |
| Saga (Orchestration) | ✅ | Luồng Place Booking gồm nhiều bước trên nhiều services. PlaceBookingService đóng vai trò Saga Orchestrator: publish Kafka commands, lắng nghe reply events, lưu Saga state, thực thi compensating transaction (cancel booking, refund) khi có bước thất bại. |
| Event-driven / Message Queue (Kafka) | ✅ | Giao tiếp bất đồng bộ giữa PlaceBookingService và các worker services (booking, payment, notification). Tăng resilience — nếu một service tạm thời down, command vẫn nằm trong Kafka topic chờ xử lý thay vì bị mất. |
| CQRS | ❌ | Không áp dụng trong scope này. Read/write pattern chưa đủ phức tạp để justify tách read model riêng. |
| Circuit Breaker | ✅ | payment-service phụ thuộc external payment gateway. Circuit Breaker (Resilience4j) ngăn cascading failure — mở circuit sau N lần timeout, trả lỗi ngay thay vì để thread pool bị block. |
| Service Registry / Discovery (Netflix Eureka) | ✅ | Tất cả services đăng ký với Eureka Server. **Server-side discovery**: Gateway query Eureka để route request, client không biết topology nội bộ. **Client-side discovery**: Internal services tự query Eureka và load balance trước khi gọi nhau qua REST. |

---

## 2. System Components

| Component | Responsibility | Tech Stack | Port |
|-----------|----------------|------------|------|
| **Frontend** | Giao diện người dùng: tìm kiếm phòng, form đặt phòng, polling trạng thái booking | React + Vite | 3000 |
| **Eureka Server** | Service Registry: nhận đăng ký từ tất cả services, cung cấp service catalog cho discovery | Spring Cloud Netflix Eureka | 8761 |
| **API Gateway** | Server-side discovery với Eureka, JWT validation, routing, CORS | Spring Cloud Gateway | 8080 |
| **place-booking-service** | Task Service — Saga Orchestrator: xác minh user (sync), publish Kafka commands, lắng nghe reply events, quản lý Saga state machine, thực thi compensating transactions | Spring Boot | 5001 |
| **user-service** | Entity Service: quản lý user profile (tên, email, thông tin liên hệ) | Spring Boot | 5002 |
| **hotel-service** | Entity Service: quản lý thông tin khách sạn, loại phòng, capacity; tính toán availability bằng cách gọi booking-service | Spring Boot | 5003 |
| **booking-service** | Entity Service: lưu trữ và quản lý booking records; cung cấp booking count cho hotel-service; cung cấp API polling trạng thái cho client | Spring Boot | 5004 |
| **payment-service** | Microservice: xử lý charge/refund qua external gateway (mock), Circuit Breaker | Spring Boot | 5005 |
| **notification-service** | Utility Service: gửi email xác nhận/huỷ phòng theo template | Spring Boot | 5006 |
| **Kafka Broker** | Message broker: trung gian bất đồng bộ giữa place-booking-service và các worker services | Apache Kafka + Zookeeper | 9092 |
| **user-db** | Database riêng cho user-service | PostgreSQL | 5432 |
| **hotel-db** | Database riêng cho hotel-service | PostgreSQL | 5433 |
| **booking-db** | Database riêng cho booking-service | PostgreSQL | 5434 |
| **payment-db** | Database riêng cho payment-service | PostgreSQL | 5435 |

---

## 3. Communication

### 3.1 Communication Styles

Hệ thống sử dụng **hai kiểu giao tiếp** với ranh giới rõ ràng:

| Kiểu | Áp dụng giữa | Lý do |
|------|-------------|-------|
| **Synchronous REST** | Client → Gateway → PlaceBookingService | Client cần nhận phản hồi ngay (202 Accepted hoặc lỗi validation/user-not-found) để hiển thị lên UI |
| **Synchronous REST** | Client → Gateway → hotel-service | Client cần data tĩnh (danh sách phòng, chi tiết khách sạn) để render UI ngay lập tức |
| **Synchronous REST** | Client → Gateway → booking-service | Client polling GET /bookings/{id} để kiểm tra trạng thái Saga sau khi nhận 202 |
| **Synchronous REST** | PlaceBookingService → user-service | Orchestrator cần xác minh user tồn tại và lấy email/tên trước khi bắt đầu Saga. Nếu user không hợp lệ → trả lỗi ngay cho client, không publish event nào |
| **Synchronous REST** | hotel-service → booking-service | hotel-service cần count booking hiện tại đồng bộ để tính availability và trả kết quả ngay cho client đang chờ |
| **Asynchronous Kafka** | PlaceBookingService ↔ booking-service | Orchestrator publish command, booking-service xử lý và reply event — decoupled, resilient với failure |
| **Asynchronous Kafka** | PlaceBookingService ↔ payment-service | Tăng resilience cho bước quan trọng nhất — payment gateway có thể chậm, không block orchestrator |
| **Asynchronous Kafka** | PlaceBookingService → notification-service | Fire & forget — gửi email là side effect, không cần đợi kết quả để tiếp tục |

### 3.2 Kafka Topics

| Topic | Publisher | Consumer | Event Types |
|-------|-----------|----------|-------------|
| `booking-commands` | place-booking-service | booking-service | `CreateBooking`, `ConfirmBooking`, `CancelBooking` |
| `booking-events` | booking-service | place-booking-service | `BookingCreated`, `BookingConfirmed`, `BookingCancelled` |
| `payment-commands` | place-booking-service | payment-service | `ProcessPayment`, `RefundPayment` |
| `payment-events` | payment-service | place-booking-service | `PaymentSucceeded`, `PaymentFailed`, `PaymentRefunded` |
| `notification-commands` | place-booking-service | notification-service | `SendBookingConfirmed`, `SendBookingFailed` |

### 3.3 Service Discovery

| Scenario | Pattern | Cơ chế hoạt động |
|----------|---------|-----------------|
| Client → API Gateway | **Server-side Discovery** | Client chỉ biết địa chỉ Gateway. Gateway đăng ký với Eureka và query Eureka để tìm instance của service đích, tự load balance. Client không cần biết topology nội bộ. |
| Internal service → service (REST) | **Client-side Discovery** | Service (e.g. hotel-service gọi booking-service) tự query Eureka lấy danh sách instances, dùng Spring Cloud LoadBalancer để chọn instance và gọi trực tiếp. |
| Kafka communication | N/A | Kafka broker address cấu hình tĩnh trong `.env` — Kafka là infrastructure, không phải application service cần discovery. |

### 3.4 Inter-service Communication Matrix

| From → To | place-booking | user-service | hotel-service | booking-service | payment-service | notification-service | Gateway |    Kafka     |
|-----------|:-------------:|:---:|:-------------:|:---:|:---------------:|:--------------------:|:---:|:------------:|
| **Frontend** |               | |               | |                 |                      | REST |              |
| **API Gateway** |     REST      | REST |     REST      | REST |                 |                      | |              |
| **place-booking-service** |               | REST sync |   REST sync    | Kafka |      Kafka      |        Kafka         | |   pub/sub    |
| **hotel-service** |   REST sync   | |               | REST sync |                 |                      | |              |
| **booking-service** |     Kafka     | |               | |                 |                      | |   pub/sub    |
| **payment-service** |     Kafka     | |               | |                 |                      | |   pub/sub    |
| **notification-service** |               | |               | |                 |                      | |     sub      |

> **payment-service và notification-service không expose qua Gateway** — chỉ tiếp nhận lệnh qua Kafka từ orchestrator. Đảm bảo mọi luồng đặt phòng đều đi qua Saga, không ai bypass được business logic.

---

## 4. Architecture Diagram

```mermaid
graph TB
    U([User / Browser]) --> FE[Frontend :3000]
    FE -->|REST| GW[API Gateway :8080\nJWT Validation\nServer-side Discovery]
    GW <-->|Register & Query| EUR[Eureka Server :8761\nService Registry]
    GW -->|REST| PBS[place-booking-service :5001\nSaga Orchestrator - Task Service]
    GW -->|REST| HS[hotel-service :5003\nEntity Service]
    GW -->|REST polling| BS[booking-service :5004\nEntity Service]

    PBS <-->|Register| EUR
    US <-->|Register| EUR
    HS <-->|Register| EUR
    BS <-->|Register| EUR
    PS <-->|Register| EUR
    NS <-->|Register| EUR

    PBS -->|REST sync\nclient-side discovery| US[user-service :5002\nEntity Service]
    HS -->|REST sync\nclient-side discovery| BS

    PBS -->|booking-commands| KF[Kafka Broker :9092]
    PBS -->|payment-commands| KF
    PBS -->|notification-commands| KF

    KF -->|booking-commands| BS
    KF -->|payment-commands| PS[payment-service :5005\nMicroservice + Circuit Breaker]
    KF -->|notification-commands| NS[notification-service :5006\nUtility Service]

    BS -->|booking-events| KF
    PS -->|payment-events| KF
    KF -->|booking-events| PBS
    KF -->|payment-events| PBS

    US --> UDB[(user-db :5432)]
    HS --> HDB[(hotel-db :5433)]
    BS --> BDB[(booking-db :5434)]
    PS --> PDB[(payment-db :5435)]

    style PBS fill:#f9a825,color:#000
    style US fill:#1565c0,color:#fff
    style HS fill:#1565c0,color:#fff
    style BS fill:#1565c0,color:#fff
    style PS fill:#b71c1c,color:#fff
    style NS fill:#2e7d32,color:#fff
    style GW fill:#4a148c,color:#fff
    style EUR fill:#e65100,color:#fff
    style KF fill:#263238,color:#fff
```

**Chú thích:**
- 🟡 **Vàng** — Task Service (Saga Orchestrator)
- 🔵 **Xanh dương** — Entity Service (Agnostic)
- 🔴 **Đỏ** — Microservice (isolated vì NFR)
- 🟢 **Xanh lá** — Utility Service
- 🟣 **Tím** — API Gateway
- 🟠 **Cam** — Eureka Service Registry
- ⚫ **Đen** — Kafka Broker

---

## 5. Saga Flow — Place Booking (Async Orchestration qua Kafka)

PlaceBookingService lưu **Saga state** trong DB của mình để track tiến trình qua các bước async. Client dùng **polling** để biết kết quả cuối cùng.



```mermaid
sequenceDiagram
    actor C as Client
    participant GW as Gateway
    participant PBS as place-booking-service
    participant US as user-service
    participant HS as hotel-service
    participant KF as Kafka
    participant BS as booking-service
    participant PS as payment-service
    participant NS as notification-service

    C->>GW: POST /place-booking
    GW->>PBS: Forward (JWT validated)

    rect rgb(220, 220, 255)
        Note over PBS,US: Step 1 — Verify User (Sync REST)
        PBS->>US: GET /users/{userId}
        alt User không tồn tại
            US-->>PBS: 404 Not Found
            PBS-->>C: 404 User not found
        else User hợp lệ
            US-->>PBS: { userId, name, email }
        end
    end

    rect rgb(255, 240, 200)
        Note over PBS,HS: Step 2 — Check Availability (Sync REST, fast-fail)
        PBS->>HS: GET /hotels/{hotelId}/room-types/{roomTypeId}/availability?checkin=&checkout=
        Note over HS: hotel-service gọi booking-service lấy active count
        alt Không còn phòng
            HS-->>PBS: { available: false }
            PBS-->>C: 409 No rooms available
        else Còn phòng
            HS-->>PBS: { available: true, remainingRooms: N, pricePerNight: X }
        end
    end

    PBS-->>C: 202 Accepted { bookingId, message: "Đang xử lý" }
    Note over C: Bắt đầu polling GET /bookings/{bookingId}

    rect rgb(200, 230, 200)
        Note over PBS,BS: Step 3 — Create Booking (Async Kafka)
        PBS->>KF: publish "CreateBooking" → [booking-commands]
        KF->>BS: consume "CreateBooking"
        Note over BS: Re-check availability trong DB transaction<br/>(chống race condition)
        alt Race condition — hết phòng
            BS->>KF: publish "BookingFailed" → [booking-events]
            KF->>PBS: consume "BookingFailed"
            PBS->>KF: publish "SendBookingFailed" → [notification-commands]
            KF->>NS: consume → gửi email thất bại
            Note over C: Polling nhận FAILED
        else Còn phòng
            BS->>BS: INSERT booking — status: PENDING
            BS->>KF: publish "BookingCreated" → [booking-events]
            KF->>PBS: consume "BookingCreated"
            PBS->>PBS: Update Saga state → BOOKING_CREATED
        end
    end

    rect rgb(255, 220, 200)
        Note over PBS,PS: Step 4 — Process Payment (Async Kafka)
        PBS->>KF: publish "ProcessPayment" → [payment-commands]
        KF->>PS: consume "ProcessPayment"
        PS->>PS: Circuit Breaker check
        alt Payment SUCCESS
            PS->>KF: publish "PaymentSucceeded" → [payment-events]
            KF->>PBS: consume "PaymentSucceeded"
            PBS->>PBS: Update Saga state → PAYMENT_SUCCESS
        else Payment FAILED
            PS->>KF: publish "PaymentFailed" → [payment-events]
            KF->>PBS: consume "PaymentFailed"
            Note over PBS: Compensate — cancel booking
            PBS->>KF: publish "CancelBooking" → [booking-commands]
            KF->>BS: consume "CancelBooking"
            BS->>BS: Update status → CANCELLED
            BS->>KF: publish "BookingCancelled" → [booking-events]
            KF->>PBS: consume "BookingCancelled"
            PBS->>PBS: Update Saga state → PAYMENT_FAILED
            PBS->>KF: publish "SendBookingFailed" → [notification-commands]
            KF->>NS: consume → gửi email thất bại
            Note over C: Polling nhận CANCELLED
        end
    end

    rect rgb(200, 255, 220)
        Note over PBS,NS: Step 5 — Confirm (Happy Path)
        PBS->>KF: publish "ConfirmBooking" → [booking-commands]
        KF->>BS: consume "ConfirmBooking"
        BS->>BS: Update status → CONFIRMED
        BS->>KF: publish "BookingConfirmed" → [booking-events]
        KF->>PBS: consume "BookingConfirmed"
        PBS->>PBS: Update Saga state → COMPLETED
        PBS->>KF: publish "SendBookingConfirmed" → [notification-commands]
        KF->>NS: consume → gửi email xác nhận
        Note over C: Polling nhận CONFIRMED
    end

    C->>GW: GET /bookings/{bookingId}
    GW->>BS: Forward
    BS-->>C: { bookingId, status: CONFIRMED/CANCELLED, ... }
```

---

## 6. Deployment

- Tất cả services containerize bằng Docker
- Orchestrate bằng Docker Compose — single command: `docker compose up --build`
- Services giao tiếp nội bộ qua Docker Compose DNS (tên service, không dùng `localhost`)

```bash
docker compose up --build

# Verify
curl http://localhost:8761                 # Eureka Dashboard
curl http://localhost:8080/actuator/health # API Gateway
curl http://localhost:5001/health          # place-booking-service
curl http://localhost:5002/health          # user-service
curl http://localhost:5003/health          # hotel-service
curl http://localhost:5004/health          # booking-service
curl http://localhost:5005/health          # payment-service
curl http://localhost:5006/health          # notification-service
```

### Startup Order (Docker Compose depends_on)

```
1. Zookeeper
2. Kafka broker          ← depends on: Zookeeper
3. Eureka Server         ← depends on: (none)
4. Databases             ← user-db, hotel-db, booking-db, payment-db
5. user-service          ← depends on: Eureka, user-db
   hotel-service         ← depends on: Eureka, hotel-db
   booking-service       ← depends on: Eureka, booking-db, Kafka
   payment-service       ← depends on: Eureka, payment-db, Kafka
   notification-service  ← depends on: Eureka, Kafka
6. place-booking-service ← depends on: Eureka, Kafka, tất cả services trên
7. API Gateway           ← depends on: Eureka
8. Frontend              ← depends on: API Gateway
```
