CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- booking-service — PostgreSQL init script
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- ENUM TYPES
-- ────────────────────────────────────────────────────────────

CREATE TYPE booking_status_enum AS ENUM (
    'FAILED',       -- Không đặt phòng thành công, diễn ra ở bước check availability ở booking service để tránh race-condition
    'PENDING',      -- Phòng đang được giữ chỗ, chờ thanh toán tiền cọc
    'CONFIRMED',    -- Đã thanh toán tiền cọc, booking được xác nhận
    'CANCELLED',    -- Không thanh toán được trong thời hạn, đơn bị huỷ
    'CHECKEDIN',    -- Khách hàng đã check-in
    'COMPLETED'     -- Khách đã thanh toán đầy đủ, check-out, đơn hoàn thành
);

CREATE TYPE payment_method_enum AS ENUM (
    'CREDIT_CARD'       -- Thanh toán qua thẻ tín dụng
);

-- ────────────────────────────────────────────────────────────
-- TABLES
-- ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS bookings (
    id               VARCHAR(255)         PRIMARY KEY,
    customer_id      VARCHAR(255)         NOT NULL,
    hotel_id         VARCHAR(255)         NOT NULL,
    created_at       TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    checkin_date     DATE                 NOT NULL,
    checkout_date    DATE                 NOT NULL,
    num_adults       INTEGER              NOT NULL,
    total_amount     NUMERIC              NOT NULL,
    currency         VARCHAR(255)         NOT NULL DEFAULT 'VND',
    status           booking_status_enum  NOT NULL DEFAULT 'PENDING',
    payment_method   payment_method_enum,
    idempotency_key  VARCHAR(255)         UNIQUE,

    CONSTRAINT chk_dates CHECK (checkout_date > checkin_date),
    CONSTRAINT chk_num_adults CHECK (num_adults > 0),
    CONSTRAINT chk_total_amount CHECK (total_amount > 0)
);

CREATE TABLE IF NOT EXISTS booked_roomtypes (
    id              VARCHAR(255) PRIMARY KEY,
    booking_id      VARCHAR(255) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    room_type_id    VARCHAR(255) NOT NULL,
    quantity        INTEGER      NOT NULL,
    price_per_night NUMERIC      NOT NULL,
    nights          INTEGER      NOT NULL,
    subtotal        NUMERIC      NOT NULL,

    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_nights CHECK (nights > 0)
);

CREATE TABLE IF NOT EXISTS booking_info (
  booking_id      VARCHAR(255) PRIMARY KEY REFERENCES bookings(id) ON DELETE CASCADE,
  booking_detail  JSONB        NOT NULL
);

-- Outbox Pattern: event được INSERT cùng transaction với business data
-- OutboxRelay (@Scheduled) đọc bảng này và publish lên Kafka
CREATE TABLE IF NOT EXISTS outbox_events (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    topic        VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    published    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP
);

-- ────────────────────────────────────────────────────────────
-- INDEXES
-- ────────────────────────────────────────────────────────────

-- Dùng cho availability check (hotel-service gọi GET /bookings/count)
-- Query: WHERE room_type_id = ? AND status IN ('PENDING','CONFIRMED')
--        AND checkin_date < :checkout AND checkout_date > :checkin
CREATE INDEX IF NOT EXISTS idx_bookings_availability
    ON bookings(checkin_date, checkout_date, status);

CREATE INDEX IF NOT EXISTS idx_booked_roomtypes_room_type
    ON booked_roomtypes(room_type_id);

CREATE INDEX IF NOT EXISTS idx_booked_roomtypes_booking
    ON booked_roomtypes(booking_id);

CREATE INDEX IF NOT EXISTS idx_bookings_customer
    ON bookings(customer_id);

CREATE INDEX IF NOT EXISTS idx_bookings_status
    ON bookings(status);

-- Index quan trọng cho OutboxRelay: chỉ scan các event chưa publish
CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox_events(published, created_at)
    WHERE published = FALSE;

-- ────────────────────────────────────────────────────────────
-- SEED DATA — Bookings (demo data)
-- Dùng các user/hotel/room từ user-service và hotel-service seed
-- ────────────────────────────────────────────────────────────

-- INSERT INTO bookings (id, customer_id, hotel_id, checkin_date, checkout_date, num_adults, total_amount, currency, status, payment_method, idempotency_key)
-- VALUES
--     -- CONFIRMED bookings (đã hoàn thành thanh toán cọc)
--     (
--         'BK-001', 'US-001', 'HT-001',
--         CURRENT_DATE + 7, CURRENT_DATE + 10,
--         2, 6600000, 'VND', 'CONFIRMED', 'BANKING',
--         'idem-BK-001'
--     ),
--     (
--         'BK-002', 'US-002', 'HT-002',
--         CURRENT_DATE + 14, CURRENT_DATE + 17,
--         2, 13500000, 'VND', 'CONFIRMED', 'E-WALLET',
--         'idem-BK-002'
--     ),
--     (
--         'BK-003', 'US-003', 'HT-003',
--         CURRENT_DATE + 3, CURRENT_DATE + 5,
--         2, 6400000, 'VND', 'CONFIRMED', 'BANKING',
--         'idem-BK-003'
--     ),
--     -- PENDING booking (đang chờ thanh toán)
--     (
--         'BK-004', 'US-004', 'HT-004',
--         CURRENT_DATE + 20, CURRENT_DATE + 23,
--         2, 3600000, 'VND', 'PENDING', NULL,
--         'idem-BK-004'
--     ),
--     -- CANCELLED booking (thanh toán thất bại)
--     (
--         'BK-005', 'US-005', 'HT-001',
--         CURRENT_DATE + 5, CURRENT_DATE + 7,
--         1, 4400000, 'VND', 'CANCELLED', NULL,
--         'idem-BK-005'
--     )
-- ON CONFLICT (id) DO NOTHING;
--
-- -- ────────────────────────────────────────────────────────────
-- -- SEED DATA — Booked Rooms
-- -- ────────────────────────────────────────────────────────────
--
-- INSERT INTO booked_rooms (id, booking_id, room_type_id, quantity, price_per_night, nights, subtotal)
-- VALUES
--     -- BK-001: 3 đêm Deluxe Room (RT-002) tại Marriott Hanoi
--     ('BR-001', 'BK-001', 'RT-002', 1, 2200000, 3, 6600000),
--
--     -- BK-002: 3 đêm Hillside Room (RT-005) tại Intercontinental Danang
--     ('BR-002', 'BK-002', 'RT-005', 1, 4500000, 3, 13500000),
--
--     -- BK-003: 2 đêm Park Room (RT-006) tại Park Hyatt Saigon
--     ('BR-003', 'BK-003', 'RT-006', 1, 3200000, 2, 6400000),
--
--     -- BK-004: 3 đêm Garden Room (RT-008) tại La Siesta Hoi An
--     ('BR-004', 'BK-004', 'RT-008', 1, 1200000, 3, 3600000),
--
--     -- BK-005: 2 đêm Superior Room (RT-001) tại Marriott Hanoi
--     ('BR-005', 'BK-005', 'RT-001', 1, 1500000, 2, 3000000)
-- ON CONFLICT (id) DO NOTHING;
