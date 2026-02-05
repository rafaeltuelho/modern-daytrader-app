package com.daytrader.common.util;

import java.math.BigDecimal;

/**
 * Shared configuration and constants for DayTrader
 */
public final class TradeConfig {
    
    private TradeConfig() {
        // Utility class - prevent instantiation
    }
    
    // Trading constants
    public static final BigDecimal DEFAULT_ORDER_FEE = new BigDecimal("15.95");
    public static final BigDecimal MIN_ORDER_FEE = new BigDecimal("5.00");
    public static final BigDecimal MAX_ORDER_FEE = new BigDecimal("100.00");
    
    public static final int DEFAULT_MARKET_SUMMARY_SIZE = 5;
    public static final int MAX_QUERY_TOP_N = 100;
    public static final int MAX_HOLDINGS_PER_ACCOUNT = 1000;
    
    // Account constants
    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("100000.00");
    public static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;
    
    // Quote constants
    public static final double MIN_QUOTE_PRICE = 1.0;
    public static final double MAX_QUOTE_PRICE = 500.0;
    public static final double MAX_PRICE_CHANGE_PERCENT = 10.0;
    
    // Order types
    public static final String ORDER_TYPE_BUY = "buy";
    public static final String ORDER_TYPE_SELL = "sell";
    
    // Order statuses
    public static final String ORDER_STATUS_OPEN = "open";
    public static final String ORDER_STATUS_PROCESSING = "processing";
    public static final String ORDER_STATUS_CLOSED = "closed";
    public static final String ORDER_STATUS_COMPLETED = "completed";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}

