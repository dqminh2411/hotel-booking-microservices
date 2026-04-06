-- ============================================================
-- hotel-service — PostgreSQL init script
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- TABLES
-- ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS hotels (
    id          VARCHAR(255) PRIMARY KEY,
    name        TEXT         NOT NULL,
    description TEXT,
    host_id     VARCHAR(255) NOT NULL,
    address     TEXT         NOT NULL,
    image_url   TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room_types (
    id                   VARCHAR(255) PRIMARY KEY,
    hotel_id             VARCHAR(255) NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    name                 TEXT         NOT NULL,
    description          TEXT,
    max_guests           INTEGER      NOT NULL,
    bed_counts           INTEGER      NOT NULL,
    base_price_per_night NUMERIC      NOT NULL,
    quantity             INTEGER      NOT NULL,
    area                 NUMERIC,
    image_url            TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
-- INDEXES
-- ────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_hotels_host_id        ON hotels(host_id);
CREATE INDEX IF NOT EXISTS idx_hotels_name           ON hotels USING gin(to_tsvector('simple', name));
CREATE INDEX IF NOT EXISTS idx_hotels_address        ON hotels USING gin(to_tsvector('simple', address));
CREATE INDEX IF NOT EXISTS idx_room_types_hotel_id   ON room_types(hotel_id);

-- ────────────────────────────────────────────────────────────
-- SEED DATA — Hotels
-- ────────────────────────────────────────────────────────────

INSERT INTO hotels (id, name, description, host_id, address, image_url, created_at) VALUES
    (
        'HT-001',
        'Marriott Hanoi',
        'Khách sạn 5 sao sang trọng tọa lạc tại trung tâm Hà Nội, cách Hồ Hoàn Kiếm 5 phút đi bộ.',
        'US-H01',
        '12 Phan Chu Trinh, Hoan Kiem, Hanoi',
        'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0e/2d/28/dc/pool.jpg?w=700&h=-1&s=1',
        NOW()
    ),
    (
        'HT-002',
        'Intercontinental Danang Sun Peninsula',
        'Khu nghỉ dưỡng 5 sao đẳng cấp thế giới trên bán đảo Sơn Trà, view biển tuyệt đẹp.',
        'US-H01',
        'Bai Bac, Son Tra Peninsula, Da Nang',
        'https://images.pexels.com/photos/258154/pexels-photo-258154.jpeg?cs=srgb&dl=pexels-pixabay-258154.jpg&fm=jpg',
        NOW()
    ),
    (
        'HT-003',
        'Park Hyatt Saigon',
        'Khách sạn boutique 5 sao phong cách Pháp cổ điển tại trung tâm TP. Hồ Chí Minh.',
        'US-H02',
        '2 Lam Son Square, District 1, Ho Chi Minh City',
        'https://images.pexels.com/photos/2034335/pexels-photo-2034335.jpeg?cs=srgb&dl=architecture-building-chairs-2034335.jpg&fm=jpg',
        NOW()
    ),
    (
        'HT-004',
        'La Siesta Hoi An Resort',
        'Resort 4 sao yên tĩnh gần phố cổ Hội An, thiết kế kết hợp kiến trúc Việt truyền thống.',
        'US-H02',
        'Cam Ha, Hoi An, Quang Nam',
        'https://wallpaperaccess.com/full/3434639.jpg',
        NOW()
    )
ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────
-- SEED DATA — Room Types
-- HT-001: Marriott Hanoi
-- ────────────────────────────────────────────────────────────

INSERT INTO room_types (id, hotel_id, name, description, max_guests, bed_counts, base_price_per_night, quantity, area, image_url) VALUES
    (
        'RT-001',
        'HT-001',
        'Superior Room',
        'Phòng tiêu chuẩn hiện đại với view thành phố, đầy đủ tiện nghi.',
        2, 1, 1500000, 15, 28,
        'https://wallpaperaccess.com/full/2690753.jpg'
    ),
    (
        'RT-002',
        'HT-001',
        'Deluxe Room',
        'Phòng rộng hơn với view hồ Hoàn Kiếm, nội thất cao cấp.',
        2, 1, 2200000, 10, 35,
        'https://image.nuprop.my/small_light(da=l,ds=s,cc=f5f5f5,autoorient=y,progressive=y,rmprof=y,of=jpg,cw=800,ch=600,dh=600)/nuprop-production/2805a0add39a76855e0fe0abf2b45f7f_800_600.jpg'
    ),
    (
        'RT-003',
        'HT-001',
        'Junior Suite',
        'Suite sang trọng với phòng khách riêng biệt và bồn tắm đứng.',
        3, 1, 3800000, 3, 55,
        'https://image.nuprop.my/small_light(da=l,ds=s,cc=f5f5f5,autoorient=y,progressive=y,rmprof=y,of=jpg,cw=800,ch=600,dh=600)/nuprop-production/2805a0add39a76855e0fe0abf2b45f7f_800_600.jpg'
    ),

-- HT-002: Intercontinental Danang
    (
        'RT-004',
        'HT-002',
        'Ocean View Villa',
        'Villa view biển riêng tư với bể bơi tràn, tầm nhìn 180 độ ra Biển Đông.',
        2, 1, 8500000, 8, 80,
        'https://example.com/rooms/intercontinental-ocean-villa.jpg'
    ),
    (
        'RT-005',
        'HT-002',
        'Hillside Room',
        'Phòng sườn đồi yên tĩnh, view xanh mướt, phong cách nhiệt đới.',
        2, 1, 4500000, 12, 45,
        'https://example.com/rooms/intercontinental-hillside.jpg'
    ),

-- HT-003: Park Hyatt Saigon
    (
        'RT-006',
        'HT-003',
        'Park Room',
        'Phòng hướng công viên Lam Sơn, phong cách Pháp cổ điển sang trọng.',
        2, 1, 3200000, 20, 40,
        'https://example.com/rooms/park-hyatt-park-room.jpg'
    ),
    (
        'RT-007',
        'HT-003',
        'Opera Suite',
        'Suite cao cấp view Nhà hát Thành phố, phòng khách rộng rãi.',
        3, 1, 7000000, 4, 85,
        'https://example.com/rooms/park-hyatt-opera-suite.jpg'
    ),

-- HT-004: La Siesta Hoi An
    (
        'RT-008',
        'HT-004',
        'Garden Room',
        'Phòng nhìn ra khu vườn nhiệt đới, thiết kế mộc mạc Hội An.',
        2, 1, 1200000, 18, 32,
        'https://example.com/rooms/la-siesta-garden.jpg'
    ),
    (
        'RT-009',
        'HT-004',
        'Pool Access Room',
        'Phòng có cửa ra thẳng hồ bơi, lý tưởng cho kỳ nghỉ gia đình.',
        4, 2, 2000000, 6, 48,
        'https://example.com/rooms/la-siesta-pool.jpg'
    )
ON CONFLICT (id) DO NOTHING;
