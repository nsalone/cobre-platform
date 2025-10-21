CREATE TABLE transactions
(
    id              CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    account_id      VARCHAR(50)         NOT NULL,
    event_id        VARCHAR(100)        NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    operation_type  VARCHAR(10)         NOT NULL, -- DEBIT/CREDIT
    amount          DECIMAL(19, 4)      NOT NULL,
    currency        VARCHAR(3)          NOT NULL,
    balance_after   DECIMAL(19, 4)      NOT NULL,
    operation_date  TIMESTAMP           NOT NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id) REFERENCES accounts (account_id)
);

CREATE INDEX idx_transactions_idempotency ON transactions (idempotency_key);
CREATE INDEX idx_transactions_account ON transactions (account_id);
