# API Gateway

## Tổng quan

Gateway sử dụng **Spring Cloud Gateway + Spring WebFlux** và **Eureka Discovery Client**.  
Việc định tuyến (routing) được cấu hình thủ công trong file `src/main/resources/application.yml`, sử dụng `lb://` để thực hiện cân bằng tải thông qua Eureka.

## Bảng định tuyến (Routing tường minh)

| Đường dẫn bên ngoài | Route ID | Service đích (Eureka) | Filter |
|---|---|---|---|
| `/place-booking/**` | `place-booking-service-route` | `lb://place-booking-service` | `StripPrefix=1` |
| `/bookings/**` | `booking-service-route` | `lb://booking-service` | `StripPrefix=1` |

## Cấu hình Eureka

- `eureka.client.service-url.defaultZone=http://localhost:8761/eureka`
- Có thể ghi đè thông qua biến môi trường: `EUREKA_SERVER_URL`

## Chạy ứng dụng

```bash
docker compose up gateway --build
