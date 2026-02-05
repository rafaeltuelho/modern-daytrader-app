-- ============================================
-- DayTrader Account Service - Initial Schema
-- Migration: V1.0.0
-- ============================================

-- Account Profile Table
CREATE TABLE account_profile (
    user_id         VARCHAR(255) PRIMARY KEY,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    address         VARCHAR(500),
    email           VARCHAR(255),
    credit_card     VARCHAR(255),
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Account Table with sequence
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE account (
    id              BIGINT PRIMARY KEY DEFAULT nextval('account_id_seq'),
    profile_user_id VARCHAR(255) NOT NULL REFERENCES account_profile(user_id) ON DELETE CASCADE,
    login_count     INTEGER DEFAULT 0 NOT NULL,
    logout_count    INTEGER DEFAULT 0 NOT NULL,
    last_login      TIMESTAMP WITH TIME ZONE,
    creation_date   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    balance         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    open_balance    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT account_profile_unique UNIQUE (profile_user_id)
);

-- Indexes
CREATE INDEX idx_account_profile_user_id ON account(profile_user_id);
CREATE INDEX idx_account_profile_email ON account_profile(email);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_account_profile_updated_at
    BEFORE UPDATE ON account_profile
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_updated_at
    BEFORE UPDATE ON account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

