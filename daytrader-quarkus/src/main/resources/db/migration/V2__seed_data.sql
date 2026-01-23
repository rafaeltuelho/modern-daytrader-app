-- =============================================================================
-- DayTrader Seed Data - Flyway Migration V2
-- Inserts sample quotes and test accounts for development/testing
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Stock Quotes (20 popular stocks)
-- -----------------------------------------------------------------------------
INSERT INTO quoteejb (symbol, companyname, volume, price, open1, low, high, change1) VALUES
    ('AAPL', 'Apple Inc.', 75000000, 185.50, 184.00, 183.25, 187.00, 1.50),
    ('GOOGL', 'Alphabet Inc.', 25000000, 141.80, 140.50, 139.75, 143.00, 1.30),
    ('MSFT', 'Microsoft Corporation', 35000000, 378.25, 375.00, 374.50, 380.00, 3.25),
    ('AMZN', 'Amazon.com Inc.', 45000000, 178.50, 176.00, 175.25, 180.00, 2.50),
    ('META', 'Meta Platforms Inc.', 20000000, 505.75, 500.00, 498.50, 510.00, 5.75),
    ('NVDA', 'NVIDIA Corporation', 55000000, 875.50, 865.00, 860.00, 885.00, 10.50),
    ('TSLA', 'Tesla Inc.', 85000000, 245.00, 242.00, 240.50, 248.00, 3.00),
    ('BRK.B', 'Berkshire Hathaway Inc.', 3500000, 408.25, 405.00, 404.00, 410.00, 3.25),
    ('JPM', 'JPMorgan Chase & Co.', 12000000, 195.50, 193.00, 192.50, 197.00, 2.50),
    ('V', 'Visa Inc.', 8000000, 278.75, 276.00, 275.25, 280.00, 2.75),
    ('JNJ', 'Johnson & Johnson', 7500000, 156.25, 155.00, 154.50, 157.00, 1.25),
    ('WMT', 'Walmart Inc.', 9000000, 165.50, 163.50, 163.00, 167.00, 2.00),
    ('PG', 'Procter & Gamble Co.', 6500000, 158.00, 156.50, 156.00, 159.00, 1.50),
    ('MA', 'Mastercard Inc.', 4500000, 458.25, 455.00, 454.00, 460.00, 3.25),
    ('HD', 'The Home Depot Inc.', 5000000, 365.75, 362.00, 361.00, 368.00, 3.75),
    ('DIS', 'The Walt Disney Company', 15000000, 112.50, 110.00, 109.50, 114.00, 2.50),
    ('NFLX', 'Netflix Inc.', 8500000, 628.00, 620.00, 618.00, 632.00, 8.00),
    ('INTC', 'Intel Corporation', 32000000, 43.75, 42.50, 42.00, 44.50, 1.25),
    ('AMD', 'Advanced Micro Devices Inc.', 48000000, 178.50, 175.00, 174.00, 180.00, 3.50),
    ('CRM', 'Salesforce Inc.', 7000000, 265.25, 262.00, 261.00, 267.00, 3.25);

-- -----------------------------------------------------------------------------
-- Test User Account Profiles
-- -----------------------------------------------------------------------------
INSERT INTO accountprofileejb (userid, passwd, fullname, address, email, creditcard) VALUES
    ('uid:0', 'xxx', 'John Doe', '123 Main St, New York, NY 10001', 'john.doe@example.com', '4111-1111-1111-1111'),
    ('uid:1', 'xxx', 'Jane Smith', '456 Oak Ave, San Francisco, CA 94102', 'jane.smith@example.com', '4222-2222-2222-2222'),
    ('uid:2', 'xxx', 'Bob Johnson', '789 Pine Rd, Chicago, IL 60601', 'bob.johnson@example.com', '4333-3333-3333-3333');

-- -----------------------------------------------------------------------------
-- Test User Accounts
-- Starting balances: $100,000 each
-- -----------------------------------------------------------------------------
INSERT INTO accountejb (id, logincount, logoutcount, lastlogin, creationdate, balance, openbalance, profile_userid) VALUES
    (1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100000.00, 100000.00, 'uid:0'),
    (2, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100000.00, 100000.00, 'uid:1'),
    (3, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100000.00, 100000.00, 'uid:2');

-- Reset sequence to continue after seeded data
SELECT setval('accountejb_id_seq', 3, true);

-- -----------------------------------------------------------------------------
-- Sample Holdings for test accounts
-- -----------------------------------------------------------------------------
INSERT INTO holdingejb (id, quantity, purchaseprice, purchasedate, account_accountid, quote_symbol) VALUES
    -- John Doe's holdings
    (1, 100, 180.00, CURRENT_TIMESTAMP - INTERVAL '30 days', 1, 'AAPL'),
    (2, 50, 370.00, CURRENT_TIMESTAMP - INTERVAL '25 days', 1, 'MSFT'),
    (3, 25, 850.00, CURRENT_TIMESTAMP - INTERVAL '20 days', 1, 'NVDA'),
    -- Jane Smith's holdings
    (4, 75, 175.00, CURRENT_TIMESTAMP - INTERVAL '28 days', 2, 'AMZN'),
    (5, 30, 140.00, CURRENT_TIMESTAMP - INTERVAL '22 days', 2, 'GOOGL'),
    (6, 40, 240.00, CURRENT_TIMESTAMP - INTERVAL '15 days', 2, 'TSLA'),
    -- Bob Johnson's holdings
    (7, 200, 42.00, CURRENT_TIMESTAMP - INTERVAL '35 days', 3, 'INTC'),
    (8, 60, 170.00, CURRENT_TIMESTAMP - INTERVAL '18 days', 3, 'AMD');

-- Reset sequence to continue after seeded data
SELECT setval('holdingejb_id_seq', 8, true);

-- -----------------------------------------------------------------------------
-- Sample Orders for test accounts (mix of completed and open orders)
-- -----------------------------------------------------------------------------
INSERT INTO orderejb (id, ordertype, orderstatus, opendate, completiondate, quantity, price, orderfee, account_accountid, quote_symbol, holding_holdingid) VALUES
    -- John Doe's orders (completed buys for holdings)
    (1, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 100, 180.00, 9.99, 1, 'AAPL', 1),
    (2, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '25 days', 50, 370.00, 9.99, 1, 'MSFT', 2),
    (3, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '20 days', 25, 850.00, 9.99, 1, 'NVDA', 3),
    -- John Doe's open order
    (4, 'buy', 'open', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, 20, 505.00, 9.99, 1, 'META', NULL),
    -- Jane Smith's orders (completed buys)
    (5, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '28 days', CURRENT_TIMESTAMP - INTERVAL '28 days', 75, 175.00, 9.99, 2, 'AMZN', 4),
    (6, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP - INTERVAL '22 days', 30, 140.00, 9.99, 2, 'GOOGL', 5),
    (7, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 40, 240.00, 9.99, 2, 'TSLA', 6),
    -- Bob Johnson's orders (completed buys)
    (8, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP - INTERVAL '35 days', 200, 42.00, 9.99, 3, 'INTC', 7),
    (9, 'buy', 'closed', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '18 days', 60, 170.00, 9.99, 3, 'AMD', 8),
    -- Bob Johnson's open sell order
    (10, 'sell', 'open', CURRENT_TIMESTAMP - INTERVAL '2 days', NULL, 50, 44.00, 9.99, 3, 'INTC', NULL);

-- Reset sequence to continue after seeded data
SELECT setval('orderejb_id_seq', 10, true);

