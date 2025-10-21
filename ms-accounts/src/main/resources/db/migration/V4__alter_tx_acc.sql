ALTER TABLE transactions ADD CONSTRAINT uq_idempotency UNIQUE (idempotency_key);
ALTER TABLE accounts MODIFY balance DECIMAL(20,4);