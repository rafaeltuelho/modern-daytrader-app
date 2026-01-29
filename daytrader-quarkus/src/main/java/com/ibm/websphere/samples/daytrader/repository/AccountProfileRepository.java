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
package com.ibm.websphere.samples.daytrader.repository;

import java.util.Optional;

import com.ibm.websphere.samples.daytrader.entity.AccountProfile;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for AccountProfile entity using Panache Repository pattern
 * Per Phase 3: Backend Migration specification section 3.2
 */
@ApplicationScoped
public class AccountProfileRepository implements PanacheRepository<AccountProfile> {

    /**
     * Find profile by user ID
     */
    public Optional<AccountProfile> findByUserID(String userID) {
        return find("userID", userID).firstResultOptional();
    }

    /**
     * Authenticate user with password
     */
    public Optional<AccountProfile> authenticate(String userID, String password) {
        return find("userID = ?1 and password = ?2", userID, password).firstResultOptional();
    }

    /**
     * Check if user ID exists
     */
    public boolean existsByUserID(String userID) {
        return count("userID", userID) > 0;
    }
}

