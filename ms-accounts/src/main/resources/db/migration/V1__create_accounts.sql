CREATE TABLE accounts
(
    id         CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    account_id VARCHAR(50) UNIQUE NOT NULL,
    currency   VARCHAR(3)         NOT NULL,
    balance    DECIMAL(19, 4)     NOT NULL DEFAULT 0,
    created_at TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP          NOT NULL DEFAULT NOW()
);