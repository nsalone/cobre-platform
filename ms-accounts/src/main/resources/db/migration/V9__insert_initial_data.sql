INSERT INTO accounts (id, account_id, currency, balance, created_at, updated_at)
VALUES
    (UUID(), 'ACC987654321', 'USD', 0.00, NOW(), NOW()),
    (UUID(), 'ACC987654344', 'EUR', 0.00, NOW(), NOW()),
    (UUID(), 'ACC987654330', 'ARS', 200000.00, NOW(), NOW()),
    (UUID(), 'ACC123456789', 'MXN', 200000.00, NOW(), NOW());
