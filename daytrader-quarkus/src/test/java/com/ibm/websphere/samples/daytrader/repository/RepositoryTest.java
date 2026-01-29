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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entity.Account;
import com.ibm.websphere.samples.daytrader.entity.AccountProfile;
import com.ibm.websphere.samples.daytrader.entity.Quote;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Repository integration tests using @QuarkusTest
 * Tests CRUD operations and custom query methods with H2 database
 * Per Phase 3: Backend Migration specification section 9 - Testing Strategy
 */
@QuarkusTest
class RepositoryTest {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository accountProfileRepository;

    @Inject
    QuoteRepository quoteRepository;

    @Inject
    HoldingRepository holdingRepository;

    @Inject
    OrderRepository orderRepository;

    // AccountProfile Repository Tests
    @Test
    @Transactional
    void testAccountProfilePersistAndFind() {
        AccountProfile profile = new AccountProfile("testuser1", "password123",
                                                    "Test User", "123 Test St",
                                                    "test@example.com", "1234-5678");

        accountProfileRepository.persist(profile);

        Optional<AccountProfile> found = accountProfileRepository.findByUserID("testuser1");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    @Transactional
    void testAccountProfileAuthenticate() {
        AccountProfile profile = new AccountProfile("authuser", "password123", 
                                                    "Auth User", "123 Auth St", 
                                                    "auth@example.com", "1234-5678");
        
        accountProfileRepository.persist(profile);
        
        Optional<AccountProfile> authenticated = accountProfileRepository.authenticate("authuser", "password123");
        assertTrue(authenticated.isPresent());
        
        Optional<AccountProfile> failed = accountProfileRepository.authenticate("authuser", "wrongpassword");
        assertFalse(failed.isPresent());
    }

    @Test
    @Transactional
    void testAccountProfileUpdate() {
        AccountProfile profile = new AccountProfile("updateuser", "password123",
                                                    "Update User", "123 Update St",
                                                    "update@example.com", "1234-5678");

        accountProfileRepository.persist(profile);

        profile.setFullName("Updated Name");
        accountProfileRepository.persist(profile);

        Optional<AccountProfile> found = accountProfileRepository.findByUserID("updateuser");
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getFullName());
    }

    // Quote Repository Tests
    @Test
    @Transactional
    void testQuotePersistAndFindBySymbol() {
        Quote quote = new Quote("TEST", "Test Company", 1000000.0, 
                               new BigDecimal("100.00"), new BigDecimal("99.00"), 
                               new BigDecimal("98.00"), new BigDecimal("101.00"), 1.0);
        
        quoteRepository.persist(quote);
        
        Optional<Quote> found = quoteRepository.findBySymbol("TEST");
        assertTrue(found.isPresent());
        assertEquals("Test Company", found.get().getCompanyName());
        assertEquals(new BigDecimal("100.00"), found.get().getPrice());
    }

    @Test
    @Transactional
    void testQuoteFindAll() {
        Quote quote1 = new Quote("TEST1", "Test Company 1", 1000000.0, 
                                new BigDecimal("100.00"), new BigDecimal("99.00"), 
                                new BigDecimal("98.00"), new BigDecimal("101.00"), 1.0);
        Quote quote2 = new Quote("TEST2", "Test Company 2", 2000000.0, 
                                new BigDecimal("200.00"), new BigDecimal("199.00"), 
                                new BigDecimal("198.00"), new BigDecimal("201.00"), 2.0);
        
        quoteRepository.persist(quote1);
        quoteRepository.persist(quote2);
        
        List<Quote> quotes = quoteRepository.findAllQuotes();
        assertTrue(quotes.size() >= 2);
    }

    @Test
    @Transactional
    void testQuoteUpdate() {
        Quote quote = new Quote("UPDT", "Update Company", 1000000.0, 
                               new BigDecimal("100.00"), new BigDecimal("99.00"), 
                               new BigDecimal("98.00"), new BigDecimal("101.00"), 1.0);
        
        quoteRepository.persist(quote);
        
        quote.setPrice(new BigDecimal("105.00"));
        quote.setChange(5.0);
        quoteRepository.persist(quote);
        
        Optional<Quote> found = quoteRepository.findBySymbol("UPDT");
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("105.00"), found.get().getPrice());
        assertEquals(5.0, found.get().getChange());
    }
}

