package com.daytrader.market.exception;

import com.daytrader.common.dto.ErrorResponse;
import com.daytrader.common.exception.BusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * JAX-RS ExceptionMapper for BusinessException
 * Maps business logic exceptions to HTTP 400 Bad Request
 */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {
    
    private static final Logger LOG = Logger.getLogger(BusinessExceptionMapper.class);
    
    @Override
    public Response toResponse(BusinessException exception) {
        LOG.debugf("Mapping BusinessException: %s - %s", exception.getErrorCode(), exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getErrorCode(),
            exception.getMessage()
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}

