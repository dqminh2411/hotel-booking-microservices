-- ============================================================
-- user-service — PostgreSQL init script
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- TABLES
-- ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS roles (
    id          VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id         VARCHAR(255) PRIMARY KEY,
    name       TEXT         NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(255) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ────────────────────────────────────────────────────────────
-- INDEXES
-- ────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_users_email     ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);

-- ────────────────────────────────────────────────────────────
-- SEED DATA — Roles
-- ────────────────────────────────────────────────────────────

INSERT INTO roles (id, name, description) VALUES
    ('RO-001', 'CUSTOMER', 'Khách hàng đặt phòng'),
    ('RO-002', 'HOST',     'Chủ khách sạn đăng bài'),
    ('RO-003', 'ADMIN',    'Quản trị viên hệ thống')
ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────
-- SEED DATA — Users
-- ────────────────────────────────────────────────────────────

INSERT INTO users (id, name, email, phone, created_at) VALUES
    ('US-001', 'Nguyen Van An',   'an.nguyen@email.com',   '0901234561', NOW()),
    ('US-002', 'Tran Thi Binh',   'binh.tran@email.com',   '0901234562', NOW()),
    ('US-003', 'Le Van Cuong',    'cuong.le@email.com',    '0901234563', NOW()),
    ('US-004', 'Pham Thi Dung',   'dung.pham@email.com',   '0901234564', NOW()),
    ('US-005', 'Hoang Van Em',    'em.hoang@email.com',    '0901234565', NOW()),
    -- HOST users (chủ khách sạn)
    ('US-H01', 'Nguyen Thi Host', 'host1@hotel.com',       '0911111111', NOW()),
    ('US-H02', 'Tran Van Manager','host2@hotel.com',       '0922222222', NOW())
ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────
-- SEED DATA — User Roles
-- ────────────────────────────────────────────────────────────

INSERT INTO user_roles (user_id, role_id) VALUES
    ('US-001', 'RO-001'),
    ('US-002', 'RO-001'),
    ('US-003', 'RO-001'),
    ('US-004', 'RO-001'),
    ('US-005', 'RO-001'),
    ('US-H01', 'RO-002'),
    ('US-H02', 'RO-002')
ON CONFLICT (user_id, role_id) DO NOTHING;
