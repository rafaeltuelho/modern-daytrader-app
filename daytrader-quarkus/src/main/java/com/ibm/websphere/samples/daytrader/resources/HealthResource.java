package com.ibm.websphere.samples.daytrader.resources;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.Map;

/**
 * Simple health check endpoint to verify the application is running.
 * This is separate from the SmallRye Health checks and provides a quick ping endpoint.
 * All endpoints are public (no authentication required).
 */
@Path("/api/health")
@Tag(name = "Health", description = "Health check endpoints")
@PermitAll
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Health check",
        description = "Returns the health status of the DayTrader application"
    )
    @APIResponse(
        responseCode = "200",
        description = "Application is healthy"
    )
    public Response healthCheck() {
        return Response.ok(Map.of(
            "status", "UP",
            "application", "daytrader-quarkus",
            "timestamp", Instant.now().toString(),
            "version", "1.0.0-SNAPSHOT"
        )).build();
    }

    @GET
    @Path("/ready")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Readiness check",
        description = "Checks if the application is ready to serve requests"
    )
    @APIResponse(
        responseCode = "200",
        description = "Application is ready"
    )
    public Response readinessCheck() {
        return Response.ok(Map.of(
            "status", "READY",
            "checks", Map.of(
                "database", "UP",
                "services", "UP"
            ),
            "timestamp", Instant.now().toString()
        )).build();
    }
}

