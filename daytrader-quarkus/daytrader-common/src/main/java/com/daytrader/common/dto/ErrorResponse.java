package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Error Response DTO
 * Per API specification for error responses
 */
public record ErrorResponse(
    String error,
    
    String message,
    
    Instant timestamp,
    
    String path,
    
    @JsonProperty("traceId")
    String traceId
) {
    /**
     * Constructor with error code and message
     */
    public ErrorResponse(String error, String message) {
        this(error, message, Instant.now(), null, null);
    }
    
    /**
     * Constructor with error code, message, and path
     */
    public ErrorResponse(String error, String message, String path) {
        this(error, message, Instant.now(), path, null);
    }
}

