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

import java.util.List;
import java.util.Optional;

import com.ibm.websphere.samples.daytrader.entity.Holding;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for Holding entity using Panache Repository pattern
 * Per Phase 3: Backend Migration specification section 3.2
 */
@ApplicationScoped
public class HoldingRepository implements PanacheRepository<Holding> {

    /**
     * Find holdings by account ID
     */
    public List<Holding> findByAccountId(Integer accountId) {
        return find("account.accountID", accountId).list();
    }

    /**
     * Find holdings by account ID with quote eagerly loaded
     */
    public List<Holding> findByAccountIdWithQuote(Integer accountId) {
        return find("SELECT h FROM Holding h LEFT JOIN FETCH h.quote WHERE h.account.accountID = ?1", accountId).list();
    }

    /**
     * Find holding by ID with quote eagerly loaded
     */
    public Optional<Holding> findByIdWithQuote(Integer holdingId) {
        return find("SELECT h FROM Holding h LEFT JOIN FETCH h.quote WHERE h.holdingID = ?1", holdingId)
                .firstResultOptional();
    }

    /**
     * Find holdings by account ID and symbol
     */
    public List<Holding> findByAccountIdAndSymbol(Integer accountId, String symbol) {
        return find("account.accountID = ?1 and quote.symbol = ?2", accountId, symbol).list();
    }

    /**
     * Delete holdings by account ID
     */
    public long deleteByAccountId(Integer accountId) {
        return delete("account.accountID", accountId);
    }
}

