package com.daytrader.market.exception;

import com.daytrader.common.dto.ErrorResponse;
import com.daytrader.common.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * JAX-RS ExceptionMapper for ResourceNotFoundException
 * Maps resource not found exceptions to HTTP 404 Not Found
 */
@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
    
    private static final Logger LOG = Logger.getLogger(ResourceNotFoundExceptionMapper.class);
    
    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        LOG.debugf("Mapping ResourceNotFoundException: %s", exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getErrorCode(),
            exception.getMessage()
        );
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}

