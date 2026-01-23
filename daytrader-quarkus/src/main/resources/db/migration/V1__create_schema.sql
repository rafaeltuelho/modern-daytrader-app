-- =============================================================================
-- DayTrader Database Schema - Flyway Migration V1
-- Creates all tables matching the Panache entities
-- =============================================================================

-- -----------------------------------------------------------------------------
-- AccountProfile table (must be created first as Account references it)
-- Uses userId as primary key (not auto-generated)
-- -----------------------------------------------------------------------------
CREATE TABLE accountprofileejb (
    userid VARCHAR(250) NOT NULL,
    passwd VARCHAR(250),
    fullname VARCHAR(250),
    address VARCHAR(250),
    email VARCHAR(250),
    creditcard VARCHAR(250),
    CONSTRAINT pk_accountprofileejb PRIMARY KEY (userid)
);

-- -----------------------------------------------------------------------------
-- Quote table (must be created before Holding and Order)
-- Uses symbol as primary key (not auto-generated)
-- -----------------------------------------------------------------------------
CREATE TABLE quoteejb (
    symbol VARCHAR(250) NOT NULL,
    companyname VARCHAR(250),
    volume DOUBLE PRECISION NOT NULL DEFAULT 0,
    price DECIMAL(14, 2),
    open1 DECIMAL(14, 2),
    low DECIMAL(14, 2),
    high DECIMAL(14, 2),
    change1 DOUBLE PRECISION NOT NULL DEFAULT 0,
    CONSTRAINT pk_quoteejb PRIMARY KEY (symbol)
);

-- -----------------------------------------------------------------------------
-- Account table (references AccountProfile)
-- Uses auto-generated id via Panache
-- -----------------------------------------------------------------------------
CREATE TABLE accountejb (
    id BIGSERIAL NOT NULL,
    logincount INTEGER NOT NULL DEFAULT 0,
    logoutcount INTEGER NOT NULL DEFAULT 0,
    lastlogin TIMESTAMP,
    creationdate TIMESTAMP,
    balance DECIMAL(14, 2),
    openbalance DECIMAL(14, 2),
    profile_userid VARCHAR(250),
    CONSTRAINT pk_accountejb PRIMARY KEY (id),
    CONSTRAINT fk_account_profile FOREIGN KEY (profile_userid) 
        REFERENCES accountprofileejb(userid) ON DELETE SET NULL
);

CREATE INDEX idx_account_profile ON accountejb(profile_userid);

-- -----------------------------------------------------------------------------
-- Holding table (references Account and Quote)
-- Uses auto-generated id via Panache
-- -----------------------------------------------------------------------------
CREATE TABLE holdingejb (
    id BIGSERIAL NOT NULL,
    quantity DOUBLE PRECISION NOT NULL DEFAULT 0,
    purchaseprice DECIMAL(14, 2),
    purchasedate TIMESTAMP,
    account_accountid BIGINT,
    quote_symbol VARCHAR(250),
    CONSTRAINT pk_holdingejb PRIMARY KEY (id),
    CONSTRAINT fk_holding_account FOREIGN KEY (account_accountid) 
        REFERENCES accountejb(id) ON DELETE CASCADE,
    CONSTRAINT fk_holding_quote FOREIGN KEY (quote_symbol) 
        REFERENCES quoteejb(symbol) ON DELETE SET NULL
);

CREATE INDEX idx_holding_account ON holdingejb(account_accountid);
CREATE INDEX idx_holding_quote ON holdingejb(quote_symbol);

-- -----------------------------------------------------------------------------
-- Order table (references Account, Quote, and Holding)
-- Uses auto-generated id via Panache
-- -----------------------------------------------------------------------------
CREATE TABLE orderejb (
    id BIGSERIAL NOT NULL,
    ordertype VARCHAR(50),
    orderstatus VARCHAR(50),
    opendate TIMESTAMP,
    completiondate TIMESTAMP,
    quantity DOUBLE PRECISION NOT NULL DEFAULT 0,
    price DECIMAL(14, 2),
    orderfee DECIMAL(14, 2),
    account_accountid BIGINT,
    quote_symbol VARCHAR(250),
    holding_holdingid BIGINT,
    CONSTRAINT pk_orderejb PRIMARY KEY (id),
    CONSTRAINT fk_order_account FOREIGN KEY (account_accountid) 
        REFERENCES accountejb(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_quote FOREIGN KEY (quote_symbol) 
        REFERENCES quoteejb(symbol) ON DELETE SET NULL,
    CONSTRAINT fk_order_holding FOREIGN KEY (holding_holdingid) 
        REFERENCES holdingejb(id) ON DELETE SET NULL
);

CREATE INDEX idx_order_account ON orderejb(account_accountid);
CREATE INDEX idx_order_quote ON orderejb(quote_symbol);
CREATE INDEX idx_order_status ON orderejb(orderstatus);
CREATE INDEX idx_order_holding ON orderejb(holding_holdingid);

