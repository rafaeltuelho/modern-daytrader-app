package com.ibm.websphere.samples.daytrader.services;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Inject
    AccountService accountService;

    @Inject
    AuthService authService;

    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    @Transactional
    void setUp() {
        Account.deleteAll();
        AccountProfile.deleteAll();
    }

    private Account createTestUser() {
        return authService.register(TEST_USER, TEST_PASSWORD, "Test User",
                "123 Test St", "test@example.com", "1234-5678", new BigDecimal("10000"));
    }

    @Nested
    @DisplayName("getAccount() tests")
    class GetAccountTests {

        @Test
        @DisplayName("should return account for existing user")
        void getAccountSuccess() {
            // Given
            Account created = createTestUser();

            // When
            Account account = accountService.getAccount(TEST_USER);

            // Then
            assertNotNull(account);
            assertEquals(created.id, account.id);
        }

        @Test
        @DisplayName("should return null when userID is null")
        void getAccountNullUserID() {
            assertNull(accountService.getAccount(null));
        }

        @Test
        @DisplayName("should return null when userID is blank")
        void getAccountBlankUserID() {
            assertNull(accountService.getAccount("  "));
        }

        @Test
        @DisplayName("should return null when user does not exist")
        void getAccountUserNotFound() {
            assertNull(accountService.getAccount("nonexistent"));
        }
    }

    @Nested
    @DisplayName("getAccountData() tests")
    class GetAccountDataTests {

        @Test
        @DisplayName("should return account data for existing user")
        void getAccountDataSuccess() {
            // Given
            Account created = createTestUser();

            // When
            Account account = accountService.getAccountData(TEST_USER);

            // Then
            assertNotNull(account);
            assertEquals(created.id, account.id);
            assertEquals(new BigDecimal("10000.00"), account.balance.setScale(2));
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void getAccountDataNullUserID() {
            assertThrows(BadRequestException.class, () -> accountService.getAccountData(null));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void getAccountDataUserNotFound() {
            assertThrows(NotFoundException.class, () -> accountService.getAccountData("nonexistent"));
        }
    }

    @Nested
    @DisplayName("getAccountProfile() tests")
    class GetAccountProfileTests {

        @Test
        @DisplayName("should return profile for existing user")
        void getAccountProfileSuccess() {
            // Given
            createTestUser();

            // When
            AccountProfile profile = accountService.getAccountProfile(TEST_USER);

            // Then
            assertNotNull(profile);
            assertEquals(TEST_USER, profile.userID);
            assertEquals("Test User", profile.fullName);
        }

        @Test
        @DisplayName("should return null when userID is null")
        void getAccountProfileNullUserID() {
            assertNull(accountService.getAccountProfile(null));
        }

        @Test
        @DisplayName("should return null when user does not exist")
        void getAccountProfileUserNotFound() {
            assertNull(accountService.getAccountProfile("nonexistent"));
        }
    }

    @Nested
    @DisplayName("updateAccountProfile() tests")
    class UpdateAccountProfileTests {

        @Test
        @DisplayName("should update profile successfully")
        void updateAccountProfileSuccess() {
            // Given
            createTestUser();

            // When
            AccountProfile updatedProfile = accountService.updateAccountProfile(
                    TEST_USER, "newpassword", "Updated Name", "456 New St",
                    "updated@example.com", "9999-0000");

            // Then
            assertNotNull(updatedProfile);
            assertEquals("newpassword", updatedProfile.password);
            assertEquals("Updated Name", updatedProfile.fullName);
            assertEquals("456 New St", updatedProfile.address);
            assertEquals("updated@example.com", updatedProfile.email);
            assertEquals("9999-0000", updatedProfile.creditCard);
        }

        @Test
        @DisplayName("should return null when userID is null")
        void updateAccountProfileNullUserID() {
            // The String-based overload returns null for invalid userID
            AccountProfile result = accountService.updateAccountProfile(null, "pass", "Name", "Addr", "email", "card");
            assertNull(result);
        }

        @Test
        @DisplayName("should return null when userID is blank")
        void updateAccountProfileBlankUserID() {
            // The String-based overload returns null for blank userID
            AccountProfile result = accountService.updateAccountProfile("  ", "pass", "Name", "Addr", "email", "card");
            assertNull(result);
        }

        @Test
        @DisplayName("should return null when user does not exist")
        void updateAccountProfileUserNotFound() {
            // The String-based overload returns null when user not found
            AccountProfile result = accountService.updateAccountProfile("nonexistent", "pass", "Name", "Addr", "email", "card");
            assertNull(result);
        }

        @Test
        @DisplayName("should allow partial updates with null values")
        void updateAccountProfilePartialUpdate() {
            // Given
            createTestUser();

            // When - only update password, leave others as null
            AccountProfile updatedProfile = accountService.updateAccountProfile(
                    TEST_USER, "newpassword", null, null, null, null);

            // Then
            assertNotNull(updatedProfile);
            assertEquals("newpassword", updatedProfile.password);
            // Original values should be preserved or set to null based on implementation
        }
    }

    @Nested
    @DisplayName("getAccountProfileData() tests")
    class GetAccountProfileDataTests {

        @Test
        @DisplayName("should return profile data for existing user")
        void getAccountProfileDataSuccess() {
            // Given
            createTestUser();

            // When
            AccountProfile profile = accountService.getAccountProfileData(TEST_USER);

            // Then
            assertNotNull(profile);
            assertEquals(TEST_USER, profile.userID);
            assertEquals("Test User", profile.fullName);
            assertEquals("test@example.com", profile.email);
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void getAccountProfileDataNullUserID() {
            assertThrows(BadRequestException.class, () -> accountService.getAccountProfileData(null));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void getAccountProfileDataUserNotFound() {
            assertThrows(NotFoundException.class, () -> accountService.getAccountProfileData("nonexistent"));
        }
    }
}

