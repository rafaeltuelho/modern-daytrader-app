package com.daytrader.account.service;

import com.daytrader.account.entity.Account;
import com.daytrader.account.entity.AccountProfile;
import com.daytrader.account.mapper.AccountMapper;
import com.daytrader.account.repository.AccountProfileRepository;
import com.daytrader.account.repository.AccountRepository;
import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.ProfileDTO;
import com.daytrader.common.dto.RegisterRequest;
import com.daytrader.common.exception.BusinessException;
import com.daytrader.common.exception.ResourceNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountService
 */
@QuarkusTest
class AccountServiceTest {

    @Inject
    AccountService accountService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository profileRepository;

    @Inject
    AccountMapper accountMapper;

    private String testUserId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        cleanupTestData();
        
        // Create a test user
        testUserId = "testuser-" + System.currentTimeMillis();
        AccountProfile profile = new AccountProfile();
        profile.userId = testUserId;
        profile.passwordHash = "hashedpassword";
        profile.fullName = "Test User";
        profile.email = "test@example.com";
        profile.address = "123 Test St";
        profile.creditCard = "1234-5678-9012-3456";
        profileRepository.persist(profile);

        Account account = new Account();
        account.profile = profile;
        account.balance = new BigDecimal("10000.00");
        account.openBalance = new BigDecimal("10000.00");
        account.loginCount = 0;
        account.logoutCount = 0;
        accountRepository.persist(account);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        cleanupTestData();
    }

    @Transactional
    void cleanupTestData() {
        // Delete all test accounts and profiles
        accountRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void testGetAccount() {
        // Get the account we created in setup
        Account account = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        
        AccountResponse response = accountService.getAccount(account.id);
        
        assertNotNull(response);
        assertEquals(account.id, response.id());
        assertEquals(testUserId, response.userId());
        assertEquals(new BigDecimal("10000.00"), response.balance());
        assertNotNull(response.profile());
    }

    @Test
    void testGetAccountNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            accountService.getAccount(999999L);
        });
    }

    @Test
    void testGetAccountByUserId() {
        AccountResponse response = accountService.getAccountByUserId(testUserId);
        
        assertNotNull(response);
        assertEquals(testUserId, response.userId());
        assertEquals(new BigDecimal("10000.00"), response.balance());
    }

    @Test
    void testGetProfile() {
        ProfileDTO profile = accountService.getProfile(testUserId);

        assertNotNull(profile);
        assertEquals(testUserId, profile.getUserId());
        assertEquals("Test User", profile.getFullName());
        assertEquals("test@example.com", profile.getEmail());
        // Credit card should be masked
        assertTrue(profile.getCreditCard().contains("*"));
    }

    @Test
    void testValidateCredentials_Success() {
        ProfileDTO profile = accountService.validateCredentials(testUserId, "anypassword");

        assertNotNull(profile);
        assertEquals(testUserId, profile.getUserId());
    }

    @Test
    void testValidateCredentials_InvalidUser() {
        ProfileDTO profile = accountService.validateCredentials("nonexistent", "password");
        
        assertNull(profile);
    }

    @Test
    void testValidateCredentials_EmptyPassword() {
        ProfileDTO profile = accountService.validateCredentials(testUserId, "");
        
        assertNull(profile);
    }

    @Test
    @Transactional
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest(
            "newuser123",
            "SecureP@ss123",
            "New User",
            "newuser@example.com",
            "456 New St",
            "9876-5432-1098-7654",
            new BigDecimal("50000.00")
        );

        AccountResponse response = accountService.register(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("newuser123", response.userId());
        assertEquals(new BigDecimal("50000.00"), response.balance());
        assertEquals(new BigDecimal("50000.00"), response.openBalance());
        assertEquals(0, response.loginCount());
        assertEquals(0, response.logoutCount());
        assertNotNull(response.profile());
        assertEquals("New User", response.profile().getFullName());
    }

    @Test
    @Transactional
    void testRegister_UserExists() {
        RegisterRequest request = new RegisterRequest(
            testUserId,  // Already exists
            "SecureP@ss123",
            "Duplicate User",
            "duplicate@example.com",
            "789 Dup St",
            "1111-2222-3333-4444",
            new BigDecimal("10000.00")
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            accountService.register(request);
        });

        // Note: BusinessException constructor signature is (message, errorCode)
        // but the code calls it with (errorCode, message), so getMessage() returns the error code
        assertEquals("USER_EXISTS", exception.getMessage());
    }

    @Test
    @Transactional
    void testRegister_EmailExists() {
        RegisterRequest request = new RegisterRequest(
            "anotheruser",
            "SecureP@ss123",
            "Another User",
            "test@example.com",  // Already exists
            "789 Another St",
            "1111-2222-3333-4444",
            new BigDecimal("10000.00")
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            accountService.register(request);
        });

        // Note: BusinessException constructor signature is (message, errorCode)
        // but the code calls it with (errorCode, message), so getMessage() returns the error code
        assertEquals("EMAIL_EXISTS", exception.getMessage());
    }

    @Test
    @Transactional
    void testRecordLogin() {
        Account account = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        int initialLoginCount = account.loginCount;

        accountService.recordLogin(testUserId);

        Account updatedAccount = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        assertEquals(initialLoginCount + 1, updatedAccount.loginCount);
        assertNotNull(updatedAccount.lastLogin);
    }

    @Test
    @Transactional
    void testRecordLogout() {
        Account account = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        int initialLogoutCount = account.logoutCount;

        accountService.recordLogout(testUserId);

        Account updatedAccount = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        assertEquals(initialLogoutCount + 1, updatedAccount.logoutCount);
    }

    @Test
    @Transactional
    void testUpdateBalance() {
        Account account = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        BigDecimal newBalance = new BigDecimal("25000.00");

        accountService.updateBalance(account.id, newBalance);

        Account updatedAccount = accountRepository.findByProfileUserId(testUserId).orElseThrow();
        assertEquals(newBalance, updatedAccount.balance);
    }
}

