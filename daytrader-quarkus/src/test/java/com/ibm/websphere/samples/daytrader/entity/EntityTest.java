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
package com.ibm.websphere.samples.daytrader.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Entity tests for DayTrader entities
 * Tests constructors, getters/setters, equals/hashCode, and toString
 * Per Phase 3: Backend Migration specification section 9 - Testing Strategy
 */
class EntityTest {

    @Test
    void testAccountConstructorAndGetters() {
        Integer accountID = 1;
        int loginCount = 5;
        int logoutCount = 3;
        Date lastLogin = new Date();
        Date creationDate = new Date();
        BigDecimal balance = new BigDecimal("10000.00");
        BigDecimal openBalance = new BigDecimal("10000.00");
        String profileID = "user1";

        Account account = new Account(accountID, loginCount, logoutCount, lastLogin, 
                                     creationDate, balance, openBalance, profileID);

        assertEquals(accountID, account.getAccountID());
        assertEquals(loginCount, account.getLoginCount());
        assertEquals(logoutCount, account.getLogoutCount());
        assertEquals(lastLogin, account.getLastLogin());
        assertEquals(creationDate, account.getCreationDate());
        assertEquals(balance, account.getBalance());
        assertEquals(openBalance, account.getOpenBalance());
        assertEquals(profileID, account.getProfileID());
    }

    @Test
    void testAccountSetters() {
        Account account = new Account();
        
        account.setAccountID(1);
        account.setLoginCount(5);
        account.setLogoutCount(3);
        Date lastLogin = new Date();
        account.setLastLogin(lastLogin);
        Date creationDate = new Date();
        account.setCreationDate(creationDate);
        BigDecimal balance = new BigDecimal("10000.00");
        account.setBalance(balance);
        BigDecimal openBalance = new BigDecimal("10000.00");
        account.setOpenBalance(openBalance);
        account.setProfileID("user1");

        assertEquals(1, account.getAccountID());
        assertEquals(5, account.getLoginCount());
        assertEquals(3, account.getLogoutCount());
        assertEquals(lastLogin, account.getLastLogin());
        assertEquals(creationDate, account.getCreationDate());
        assertEquals(balance, account.getBalance());
        assertEquals(openBalance, account.getOpenBalance());
        assertEquals("user1", account.getProfileID());
    }

    @Test
    void testAccountEqualsAndHashCode() {
        Account account1 = new Account();
        account1.setAccountID(1);
        
        Account account2 = new Account();
        account2.setAccountID(1);
        
        Account account3 = new Account();
        account3.setAccountID(2);

        assertEquals(account1, account2);
        assertNotEquals(account1, account3);
        assertEquals(account1.hashCode(), account2.hashCode());
    }

    @Test
    void testAccountToString() {
        Account account = new Account();
        account.setAccountID(1);
        account.setLoginCount(5);
        
        String toString = account.toString();
        assertTrue(toString.contains("accountID=1"));
        assertTrue(toString.contains("loginCount=5"));
    }

    @Test
    void testAccountLoginSuccess() {
        Account account = new Account();
        account.setAccountID(1);
        account.setLoginCount(0);
        
        AccountProfile profile = new AccountProfile();
        profile.setUserID("user1");
        profile.setPassword("password123");
        account.setProfile(profile);

        account.login("password123");
        
        assertEquals(1, account.getLoginCount());
        assertNotNull(account.getLastLogin());
    }

    @Test
    void testAccountLoginFailure() {
        Account account = new Account();
        account.setAccountID(1);
        
        AccountProfile profile = new AccountProfile();
        profile.setUserID("user1");
        profile.setPassword("password123");
        account.setProfile(profile);

        assertThrows(IllegalArgumentException.class, () -> account.login("wrongpassword"));
    }

    @Test
    void testAccountLogout() {
        Account account = new Account();
        account.setLogoutCount(0);

        account.logout();

        assertEquals(1, account.getLogoutCount());
    }

    // AccountProfile Tests
    @Test
    void testAccountProfileConstructorAndGetters() {
        String userID = "user1";
        String password = "password123";
        String fullName = "John Doe";
        String address = "123 Main St";
        String email = "john@example.com";
        String creditCard = "1234-5678-9012-3456";

        AccountProfile profile = new AccountProfile(userID, password, fullName, address, email, creditCard);

        assertEquals(userID, profile.getUserID());
        assertEquals(password, profile.getPassword());
        assertEquals(fullName, profile.getFullName());
        assertEquals(address, profile.getAddress());
        assertEquals(email, profile.getEmail());
        assertEquals(creditCard, profile.getCreditCard());
    }

    @Test
    void testAccountProfileSetters() {
        AccountProfile profile = new AccountProfile();

        profile.setUserID("user1");
        profile.setPassword("password123");
        profile.setFullName("John Doe");
        profile.setAddress("123 Main St");
        profile.setEmail("john@example.com");
        profile.setCreditCard("1234-5678-9012-3456");

        assertEquals("user1", profile.getUserID());
        assertEquals("password123", profile.getPassword());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("123 Main St", profile.getAddress());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("1234-5678-9012-3456", profile.getCreditCard());
    }

    @Test
    void testAccountProfileEqualsAndHashCode() {
        AccountProfile profile1 = new AccountProfile();
        profile1.setUserID("user1");

        AccountProfile profile2 = new AccountProfile();
        profile2.setUserID("user1");

        AccountProfile profile3 = new AccountProfile();
        profile3.setUserID("user2");

        assertEquals(profile1, profile2);
        assertNotEquals(profile1, profile3);
        assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    void testAccountProfileToString() {
        AccountProfile profile = new AccountProfile();
        profile.setUserID("user1");
        profile.setFullName("John Doe");

        String toString = profile.toString();
        assertTrue(toString.contains("userID='user1'"));
        assertTrue(toString.contains("fullName='John Doe'"));
    }

    // Quote Tests
    @Test
    void testQuoteConstructorAndGetters() {
        String symbol = "IBM";
        String companyName = "IBM Corporation";
        double volume = 1000000.0;
        BigDecimal price = new BigDecimal("150.00");
        BigDecimal open = new BigDecimal("148.00");
        BigDecimal low = new BigDecimal("147.00");
        BigDecimal high = new BigDecimal("152.00");
        double change = 2.0;

        Quote quote = new Quote(symbol, companyName, volume, price, open, low, high, change);

        assertEquals(symbol, quote.getSymbol());
        assertEquals(companyName, quote.getCompanyName());
        assertEquals(volume, quote.getVolume());
        assertEquals(price, quote.getPrice());
        assertEquals(open, quote.getOpen());
        assertEquals(low, quote.getLow());
        assertEquals(high, quote.getHigh());
        assertEquals(change, quote.getChange());
    }

    @Test
    void testQuoteSetters() {
        Quote quote = new Quote();

        quote.setSymbol("IBM");
        quote.setCompanyName("IBM Corporation");
        quote.setVolume(1000000.0);
        BigDecimal price = new BigDecimal("150.00");
        quote.setPrice(price);
        BigDecimal open = new BigDecimal("148.00");
        quote.setOpen(open);
        BigDecimal low = new BigDecimal("147.00");
        quote.setLow(low);
        BigDecimal high = new BigDecimal("152.00");
        quote.setHigh(high);
        quote.setChange(2.0);

        assertEquals("IBM", quote.getSymbol());
        assertEquals("IBM Corporation", quote.getCompanyName());
        assertEquals(1000000.0, quote.getVolume());
        assertEquals(price, quote.getPrice());
        assertEquals(open, quote.getOpen());
        assertEquals(low, quote.getLow());
        assertEquals(high, quote.getHigh());
        assertEquals(2.0, quote.getChange());
    }

    @Test
    void testQuoteEqualsAndHashCode() {
        Quote quote1 = new Quote();
        quote1.setSymbol("IBM");

        Quote quote2 = new Quote();
        quote2.setSymbol("IBM");

        Quote quote3 = new Quote();
        quote3.setSymbol("GOOG");

        assertEquals(quote1, quote2);
        assertNotEquals(quote1, quote3);
        assertEquals(quote1.hashCode(), quote2.hashCode());
    }

    @Test
    void testQuoteToString() {
        Quote quote = new Quote();
        quote.setSymbol("IBM");
        quote.setCompanyName("IBM Corporation");

        String toString = quote.toString();
        assertTrue(toString.contains("symbol='IBM'"));
        assertTrue(toString.contains("companyName='IBM Corporation'"));
    }
}
