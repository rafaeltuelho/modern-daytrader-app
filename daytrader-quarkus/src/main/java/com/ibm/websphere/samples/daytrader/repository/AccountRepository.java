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

import com.ibm.websphere.samples.daytrader.entity.Account;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for Account entity using Panache Repository pattern
 * Per Phase 3: Backend Migration specification section 3.2
 */
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    /**
     * Find account by profile user ID
     */
    public Optional<Account> findByProfileUserID(String userID) {
        return find("profile.userID", userID).firstResultOptional();
    }

    /**
     * Find account by account ID with profile eagerly loaded
     */
    public Optional<Account> findByIdWithProfile(Integer accountID) {
        return find("SELECT a FROM Account a LEFT JOIN FETCH a.profile WHERE a.accountID = ?1", accountID)
                .firstResultOptional();
    }

    /**
     * Find account by account ID with holdings eagerly loaded
     */
    public Optional<Account> findByIdWithHoldings(Integer accountID) {
        return find("SELECT a FROM Account a LEFT JOIN FETCH a.holdings WHERE a.accountID = ?1", accountID)
                .firstResultOptional();
    }

    /**
     * Find account by account ID with orders eagerly loaded
     */
    public Optional<Account> findByIdWithOrders(Integer accountID) {
        return find("SELECT a FROM Account a LEFT JOIN FETCH a.orders WHERE a.accountID = ?1", accountID)
                .firstResultOptional();
    }
}

