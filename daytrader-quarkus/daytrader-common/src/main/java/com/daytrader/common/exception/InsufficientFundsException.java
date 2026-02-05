package com.daytrader.common.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when an account has insufficient funds for a transaction
 */
public class InsufficientFundsException extends BusinessException {
    
    private final BigDecimal required;
    private final BigDecimal available;
    
    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS");
        this.required = null;
        this.available = null;
    }
    
    public InsufficientFundsException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient funds: required %s, available %s", required, available), 
              "INSUFFICIENT_FUNDS");
        this.required = required;
        this.available = available;
    }
    
    public BigDecimal getRequired() {
        return required;
    }
    
    public BigDecimal getAvailable() {
        return available;
    }
}

