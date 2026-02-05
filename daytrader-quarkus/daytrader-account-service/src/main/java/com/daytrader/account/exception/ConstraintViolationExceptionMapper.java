package com.daytrader.account.exception;

import com.daytrader.common.dto.ValidationErrorResponse;
import com.daytrader.common.dto.ValidationErrorResponse.FieldViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS ExceptionMapper for ConstraintViolationException
 * Maps Jakarta Bean Validation errors to HTTP 400 Bad Request
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    private static final Logger LOG = Logger.getLogger(ConstraintViolationExceptionMapper.class);
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        LOG.debugf("Mapping ConstraintViolationException: %d violations", exception.getConstraintViolations().size());
        
        List<FieldViolation> violations = exception.getConstraintViolations().stream()
            .map(this::toFieldViolation)
            .collect(Collectors.toList());
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            "Validation failed",
            violations
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
    
    private FieldViolation toFieldViolation(ConstraintViolation<?> violation) {
        // Extract field name from property path
        String fieldName = violation.getPropertyPath().toString();
        // Get the last part of the path (e.g., "registerAccount.arg0.email" -> "email")
        int lastDot = fieldName.lastIndexOf('.');
        if (lastDot >= 0) {
            fieldName = fieldName.substring(lastDot + 1);
        }
        
        return new FieldViolation(fieldName, violation.getMessage());
    }
}

