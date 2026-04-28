DROP TABLE transactions CASCADE CONSTRAINTS PURGE;
DROP TABLE categories CASCADE CONSTRAINTS PURGE;

CREATE TABLE categories (
    id NUMBER(19) PRIMARY KEY,
    name VARCHAR2(50) NOT NULL,
    type VARCHAR2(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT uk_category_name_type UNIQUE (name, type)
);

CREATE TABLE transactions (
    id NUMBER(19) PRIMARY KEY,
    amount NUMBER(19,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR2(3) DEFAULT 'RUB' CHECK (currency IN ('RUB', 'USD', 'EUR')),
    transaction_date DATE NOT NULL,
    category_id NUMBER(19) NOT NULL REFERENCES categories(id),
    type VARCHAR2(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    transaction_comment VARCHAR2(500),
    status VARCHAR2(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE SEQUENCE transactions_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX idx_category_type ON categories(type, is_active);
CREATE INDEX idx_trans_status_date ON transactions(status, transaction_date DESC);
CREATE INDEX idx_trans_category ON transactions(category_id, status);
CREATE INDEX idx_trans_type ON transactions(type, status, transaction_date);
