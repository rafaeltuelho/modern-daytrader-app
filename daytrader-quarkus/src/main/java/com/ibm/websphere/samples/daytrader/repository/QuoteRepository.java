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

import com.ibm.websphere.samples.daytrader.entity.Quote;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for Quote entity using Panache Repository pattern
 * Per Phase 3: Backend Migration specification section 3.2
 */
@ApplicationScoped
public class QuoteRepository implements PanacheRepository<Quote> {

    /**
     * Find quote by symbol (case-insensitive)
     */
    public Optional<Quote> findBySymbol(String symbol) {
        return find("UPPER(symbol) = UPPER(?1)", symbol).firstResultOptional();
    }

    /**
     * Find all quotes
     */
    public List<Quote> findAllQuotes() {
        return listAll();
    }

    /**
     * Find quotes by symbols
     */
    public List<Quote> findBySymbols(List<String> symbols) {
        return list("symbol in ?1", symbols);
    }

    /**
     * Find top gainers
     */
    public List<Quote> findTopGainers(int limit) {
        return find("ORDER BY change DESC").page(0, limit).list();
    }

    /**
     * Find top losers
     */
    public List<Quote> findTopLosers(int limit) {
        return find("ORDER BY change ASC").page(0, limit).list();
    }

    /**
     * Find quote for update (pessimistic lock)
     */
    public Optional<Quote> findForUpdate(String symbol) {
        return find("SELECT q FROM Quote q WHERE q.symbol = ?1", symbol)
                .withLock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional();
    }

    /**
     * Find all quotes ordered by change (descending) for market summary
     * Per Phase 2: Market Summary & Profiles
     */
    public List<Quote> findAllQuotesOrderedByChange() {
        return find("ORDER BY change DESC").list();
    }
}

