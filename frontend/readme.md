# Frontend - Demo Đặt Phòng Khách Sạn

## Tổng quan

Frontend này là ứng dụng React 1 trang (single page) phục vụ demo luồng đặt phòng:

- Hiển thị thông tin cố định của 1 khách sạn (`HT-001`)
- Hiển thị 3 loại phòng có checkbox/số lượng để người dùng chọn
- Form đặt phòng với thông tin user cố định, ngày checkin/checkout, số người lớn, thông tin thanh toán demo
- Tạo `paymentToken` giả và `idempotencyKey` cho mỗi lần submit
- Gọi API `POST /place-booking`
- Polling `GET /bookings/{bookingId}` mỗi 2 giây
- Hiển thị spinner khi `PENDING`, màn hình thành công khi `CONFIRMED`, màn hình lỗi khi `CANCELLED` (hỗ trợ thêm `FAILED`)

## Công nghệ sử dụng

| Thành phần | Lựa chọn |
| --- | --- |
| Framework | React 18 |
| Build tool | Vite 5 |
| Styling | Tailwind CSS |
| Đóng gói | npm |
| Runtime container | Nginx (serve static build) |

## Cấu trúc thư mục

```text
frontend/
├── Dockerfile
├── nginx.conf
├── index.html
├── package.json
├── postcss.config.js
├── tailwind.config.js
├── vite.config.js
├── readme.md
└── src/
    ├── App.jsx
    ├── main.jsx
    └── index.css
```

## Biến môi trường

Frontend sử dụng biến môi trường Vite:

| Biến | Mô tả | Mặc định |
| --- | --- | --- |
| `VITE_API_URL` | Base URL của API Gateway | `http://localhost:8080` |

Ví dụ trong `.env` (tại root project):

```dotenv
VITE_API_URL=http://localhost:8080
```

## Chạy local

```bash
cd frontend
npm install
npm run dev
```

Mặc định Vite chạy tại `http://localhost:5173`.

## Chạy bằng Docker Compose

Từ root project:

```bash
docker compose up frontend --build
```

Frontend expose cổng `3000`.

## Luồng API

### 1) Đặt phòng

- Endpoint: `POST /place-booking`
- Payload gửi lên backend:

```json
{
  "userId": "US-001",
  "hotelId": "HT-001",
  "roomTypeList": [
    { "roomTypeId": "RT-001", "quantity": 1, "price": 1500000 }
  ],
  "checkin": "2026-06-01",
  "checkout": "2026-06-03",
  "numAdults": 2,
  "totalAmount": 3000000,
  "currency": "VND",
  "paymentMethod": "CREDIT_CARD",
  "paymentToken": "tok_xxxxx",
  "idempotencyKey": "uuid-v4"
}
```

### 2) Polling trạng thái

Sau khi nhận `bookingId` từ `POST /place-booking`, frontend gọi:

- `GET /bookings/{bookingId}` mỗi 2 giây

Frontend dừng polling khi trạng thái là một trong các trạng thái kết thúc:

- `CONFIRMED` -> hiện màn hình thành công
- `CANCELLED` -> hiện màn hình thất bại
- `FAILED` -> hiện màn hình thất bại

## Quy tắc tính tổng tiền

`totalAmount = tong( base_price_per_night * quantity * so_dem )`

Trong đó:

- `so_dem = checkout - checkin` (theo ngày)
- Tiền tệ cố định: `VND`

## Dữ liệu demo cố định

- Khách sạn: `HT-001` - Marriott Hanoi
- User: `US-001` - Nguyen Duc Lam
- 3 room types: `RT-001`, `RT-002`, `RT-003`

## Ghi chú

- Đây là frontend demo nên thông tin thẻ (card number, CVV, ...) chỉ dùng để nhập UI, không gửi trực tiếp lên backend.
- Frontend tạo token thanh toán giả (`tok_...`) trước khi submit.
- Mỗi lần bấm **Book Now** sẽ tạo `idempotencyKey` mới để phục vụ idempotency phía backend.

