-- ============================================
-- DayTrader Trading Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Order ID Sequence
CREATE SEQUENCE order_id_seq START WITH 1 INCREMENT BY 50;

-- Orders Table (Buy/Sell orders)
CREATE TABLE orders (
    id              BIGINT PRIMARY KEY DEFAULT nextval('order_id_seq'),
    order_type      VARCHAR(10) NOT NULL,
    order_status    VARCHAR(20) NOT NULL,
    open_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    quantity        DOUBLE PRECISION NOT NULL,
    price           DECIMAL(14,2),
    order_fee       DECIMAL(14,2),
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    holding_id      BIGINT,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Orders Indexes
CREATE INDEX idx_orders_account_id ON orders(account_id);
CREATE INDEX idx_orders_symbol ON orders(quote_symbol);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_open_date ON orders(open_date);

-- Holding ID Sequence
CREATE SEQUENCE holding_id_seq START WITH 1 INCREMENT BY 50;

-- Holding Table (Stock holdings in portfolio)
CREATE TABLE holding (
    id              BIGINT PRIMARY KEY DEFAULT nextval('holding_id_seq'),
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL,
    purchase_price  DECIMAL(14,2) NOT NULL,
    purchase_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Holding Indexes
CREATE INDEX idx_holding_account_id ON holding(account_id);
CREATE INDEX idx_holding_symbol ON holding(quote_symbol);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_holding_updated_at
    BEFORE UPDATE ON holding
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

