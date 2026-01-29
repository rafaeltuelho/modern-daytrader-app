/**
 * (C) Copyright IBM Corporation 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.health;

import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Database health check
 * Verifies database connectivity by attempting a simple query
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    QuoteRepository quoteRepository;

    @Override
    public HealthCheckResponse call() {
        try {
            // Simple query to verify database connectivity
            long count = quoteRepository.count();
            return HealthCheckResponse.builder()
                    .name("Database")
                    .up()
                    .withData("quote_count", count)
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.builder()
                    .name("Database")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}

