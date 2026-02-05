-- ============================================
-- DayTrader Market Service - Seed Quote Data
-- Migration: V1.0.1
-- Description: Add 50 stock quotes with varied price changes for gainers/losers
-- ============================================

-- Clear existing data and insert fresh seed data
DELETE FROM quote;

-- Insert 50 stock quotes with realistic data
-- Top Gainers (positive price_change)
INSERT INTO quote (symbol, company_name, volume, price, open_price, low_price, high_price, price_change)
VALUES
    ('NVDA', 'NVIDIA Corporation', 45000000, 875.28, 820.00, 815.00, 880.00, 6.74),
    ('SMCI', 'Super Micro Computer', 12000000, 1024.50, 950.00, 945.00, 1030.00, 7.84),
    ('AMD', 'Advanced Micro Devices', 35000000, 178.45, 168.00, 165.00, 180.00, 6.22),
    ('META', 'Meta Platforms Inc.', 18000000, 505.50, 480.00, 475.00, 510.00, 5.31),
    ('AVGO', 'Broadcom Inc.', 8000000, 1285.00, 1220.00, 1215.00, 1290.00, 5.33),
    ('PLTR', 'Palantir Technologies', 42000000, 24.85, 23.50, 23.00, 25.00, 5.74),
    ('CRWD', 'CrowdStrike Holdings', 6500000, 312.40, 298.00, 295.00, 315.00, 4.83),
    ('PANW', 'Palo Alto Networks', 4200000, 358.90, 345.00, 342.00, 360.00, 4.03),
    ('NOW', 'ServiceNow Inc.', 3800000, 785.20, 760.00, 755.00, 790.00, 3.32),
    ('SNOW', 'Snowflake Inc.', 7500000, 198.75, 192.00, 190.00, 200.00, 3.52),

-- Moderate Gainers
    ('AAPL', 'Apple Inc.', 52000000, 185.50, 180.00, 178.00, 187.00, 3.06),
    ('MSFT', 'Microsoft Corporation', 28000000, 415.80, 405.00, 402.00, 418.00, 2.67),
    ('GOOGL', 'Alphabet Inc.', 22000000, 155.40, 152.00, 150.00, 157.00, 2.24),
    ('AMZN', 'Amazon.com Inc.', 38000000, 186.78, 183.00, 181.00, 189.00, 2.06),
    ('TSLA', 'Tesla Inc.', 85000000, 248.50, 244.00, 240.00, 252.00, 1.84),
    ('NFLX', 'Netflix Inc.', 9500000, 628.30, 618.00, 615.00, 632.00, 1.67),
    ('CRM', 'Salesforce Inc.', 7200000, 298.45, 294.00, 292.00, 300.00, 1.51),
    ('ADBE', 'Adobe Inc.', 4800000, 578.90, 572.00, 570.00, 582.00, 1.21),
    ('ORCL', 'Oracle Corporation', 12500000, 128.65, 127.00, 126.00, 130.00, 1.30),
    ('INTC', 'Intel Corporation', 45000000, 42.85, 42.30, 42.00, 43.50, 1.30),

-- Neutral/Slight Movement
    ('IBM', 'IBM Corporation', 5200000, 188.40, 187.50, 186.00, 190.00, 0.48),
    ('CSCO', 'Cisco Systems', 18000000, 52.35, 52.10, 51.80, 52.80, 0.48),
    ('QCOM', 'Qualcomm Inc.', 9800000, 168.90, 168.20, 167.00, 170.00, 0.42),
    ('TXN', 'Texas Instruments', 6500000, 172.45, 172.00, 171.00, 174.00, 0.26),
    ('MU', 'Micron Technology', 22000000, 98.75, 98.50, 97.50, 100.00, 0.25),
    ('AMAT', 'Applied Materials', 8500000, 198.30, 198.00, 196.50, 200.00, 0.15),
    ('LRCX', 'Lam Research', 3200000, 945.60, 945.00, 940.00, 950.00, 0.06),
    ('KLAC', 'KLA Corporation', 2800000, 685.40, 685.00, 682.00, 688.00, 0.06),
    ('MRVL', 'Marvell Technology', 15000000, 72.85, 72.80, 72.00, 74.00, 0.07),
    ('ADI', 'Analog Devices', 4500000, 218.90, 218.80, 217.00, 220.00, 0.05),

-- Moderate Losers
    ('V', 'Visa Inc.', 8500000, 278.90, 282.00, 277.00, 284.00, -1.10),
    ('MA', 'Mastercard Inc.', 5200000, 458.30, 464.00, 456.00, 466.00, -1.23),
    ('JPM', 'JPMorgan Chase & Co.', 12000000, 196.30, 199.00, 195.00, 201.00, -1.36),
    ('BAC', 'Bank of America', 42000000, 35.85, 36.40, 35.50, 36.80, -1.51),
    ('WFC', 'Wells Fargo & Co.', 18000000, 58.45, 59.40, 58.00, 60.00, -1.60),
    ('GS', 'Goldman Sachs', 3800000, 425.60, 433.00, 423.00, 436.00, -1.71),
    ('MS', 'Morgan Stanley', 9500000, 98.75, 100.50, 98.00, 101.50, -1.74),
    ('C', 'Citigroup Inc.', 15000000, 58.90, 60.00, 58.50, 60.80, -1.83),
    ('AXP', 'American Express', 4200000, 228.45, 233.00, 227.00, 235.00, -1.95),
    ('BLK', 'BlackRock Inc.', 1800000, 845.30, 862.00, 842.00, 868.00, -1.94),

-- Top Losers (negative price_change)
    ('WMT', 'Walmart Inc.', 14000000, 168.42, 175.00, 167.00, 176.00, -3.76),
    ('HD', 'Home Depot Inc.', 6800000, 358.90, 375.00, 356.00, 378.00, -4.29),
    ('LOW', 'Lowes Companies', 5200000, 228.45, 240.00, 226.00, 242.00, -4.81),
    ('TGT', 'Target Corporation', 8500000, 145.80, 154.00, 144.00, 156.00, -5.32),
    ('COST', 'Costco Wholesale', 4200000, 725.60, 768.00, 720.00, 772.00, -5.52),
    ('NKE', 'Nike Inc.', 12000000, 98.45, 105.00, 97.00, 106.00, -6.24),
    ('SBUX', 'Starbucks Corporation', 9800000, 92.30, 99.00, 91.00, 100.00, -6.77),
    ('MCD', 'McDonalds Corporation', 5500000, 285.60, 308.00, 283.00, 310.00, -7.27),
    ('DIS', 'Walt Disney Company', 18000000, 108.45, 118.00, 107.00, 120.00, -8.09),
    ('PEP', 'PepsiCo Inc.', 7200000, 165.80, 182.00, 164.00, 184.00, -8.90);

