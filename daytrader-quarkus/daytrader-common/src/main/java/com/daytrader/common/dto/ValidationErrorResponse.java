package com.daytrader.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Validation Error Response DTO
 * Per API specification for validation error responses
 */
public record ValidationErrorResponse(
    String error,
    
    String message,
    
    Instant timestamp,
    
    String path,
    
    List<FieldViolation> violations
) {
    /**
     * Constructor with violations
     */
    public ValidationErrorResponse(String message, List<FieldViolation> violations) {
        this("VALIDATION_ERROR", message, Instant.now(), null, violations);
    }
    
    /**
     * Constructor with message, path, and violations
     */
    public ValidationErrorResponse(String message, String path, List<FieldViolation> violations) {
        this("VALIDATION_ERROR", message, Instant.now(), path, violations);
    }
    
    /**
     * Field violation details
     */
    public record FieldViolation(
        String field,
        String message
    ) {}
}

