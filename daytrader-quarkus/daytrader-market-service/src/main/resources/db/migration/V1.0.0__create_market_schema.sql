-- ============================================
-- DayTrader Market Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Quote Table (Stock quote information)
CREATE TABLE quote (
    symbol          VARCHAR(10) PRIMARY KEY,
    company_name    VARCHAR(255),
    volume          DOUBLE PRECISION NOT NULL,
    price           DECIMAL(14,2) NOT NULL,
    open_price      DECIMAL(14,2),
    low_price       DECIMAL(14,2),
    high_price      DECIMAL(14,2),
    price_change    DOUBLE PRECISION,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Quote Index (unique on symbol already as PK)
CREATE INDEX idx_quote_symbol ON quote(symbol);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger
CREATE TRIGGER update_quote_updated_at
    BEFORE UPDATE ON quote
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed initial quote data (common stock symbols)
INSERT INTO quote (symbol, company_name, volume, price, open_price, low_price, high_price, price_change)
VALUES
    ('AAPL', 'Apple Inc.', 1000000, 180.50, 178.00, 175.50, 182.00, 2.50),
    ('GOOGL', 'Alphabet Inc.', 500000, 142.30, 140.00, 138.00, 145.00, 2.30),
    ('MSFT', 'Microsoft Corporation', 750000, 378.91, 375.00, 372.00, 380.00, 3.91),
    ('AMZN', 'Amazon.com Inc.', 600000, 186.78, 184.00, 182.00, 189.00, 2.78),
    ('META', 'Meta Platforms Inc.', 450000, 505.50, 500.00, 495.00, 510.00, 5.50),
    ('TSLA', 'Tesla Inc.', 800000, 248.50, 245.00, 240.00, 252.00, 3.50),
    ('NVDA', 'NVIDIA Corporation', 900000, 875.28, 860.00, 855.00, 880.00, 15.28),
    ('JPM', 'JPMorgan Chase & Co.', 400000, 196.30, 194.00, 192.00, 198.00, 2.30),
    ('V', 'Visa Inc.', 350000, 278.90, 275.00, 273.00, 280.00, 3.90),
    ('WMT', 'Walmart Inc.', 550000, 168.42, 166.00, 164.00, 170.00, 2.42);

