package com.ibm.websphere.samples.daytrader.services;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
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
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Inject
    AuthService authService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up before each test
        Account.deleteAll();
        AccountProfile.deleteAll();
    }

    @Nested
    @DisplayName("login() tests")
    class LoginTests {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void loginSuccess() {
            // Given
            authService.register("testuser", "password123", "Test User", 
                    "123 Test St", "test@example.com", "1234-5678", new BigDecimal("10000"));

            // When
            Account account = authService.login("testuser", "password123");

            // Then
            assertNotNull(account);
            assertEquals(1, account.loginCount);
            assertNotNull(account.lastLogin);
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void loginNullUserID() {
            assertThrows(BadRequestException.class, () -> authService.login(null, "password"));
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is blank")
        void loginBlankUserID() {
            assertThrows(BadRequestException.class, () -> authService.login("  ", "password"));
        }

        @Test
        @DisplayName("should throw BadRequestException when password is null")
        void loginNullPassword() {
            assertThrows(BadRequestException.class, () -> authService.login("user", null));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void loginUserNotFound() {
            assertThrows(NotFoundException.class, () -> authService.login("nonexistent", "password"));
        }

        @Test
        @DisplayName("should throw NotAuthorizedException when password is invalid")
        void loginInvalidPassword() {
            // Given
            authService.register("testuser", "correctpass", "Test User",
                    "123 Test St", "test@example.com", "1234-5678", new BigDecimal("10000"));

            // When/Then
            assertThrows(NotAuthorizedException.class, 
                    () -> authService.login("testuser", "wrongpass"));
        }

        @Test
        @DisplayName("should increment login count on successive logins")
        void loginIncrementsCount() {
            // Given
            authService.register("testuser", "password123", "Test User",
                    "123 Test St", "test@example.com", "1234-5678", new BigDecimal("10000"));

            // When
            authService.login("testuser", "password123");
            Account account = authService.login("testuser", "password123");

            // Then
            assertEquals(2, account.loginCount);
        }
    }

    @Nested
    @DisplayName("logout() tests")
    class LogoutTests {

        @Test
        @DisplayName("should logout successfully and increment logout count")
        void logoutSuccess() {
            // Given
            authService.register("testuser", "password123", "Test User",
                    "123 Test St", "test@example.com", "1234-5678", new BigDecimal("10000"));
            authService.login("testuser", "password123");

            // When
            authService.logout("testuser");

            // Then
            Account account = Account.findByProfileUserID("testuser");
            assertEquals(1, account.logoutCount);
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void logoutNullUserID() {
            assertThrows(BadRequestException.class, () -> authService.logout(null));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void logoutUserNotFound() {
            assertThrows(NotFoundException.class, () -> authService.logout("nonexistent"));
        }
    }

    @Nested
    @DisplayName("register() tests")
    class RegisterTests {

        @Test
        @DisplayName("should register new user successfully")
        void registerSuccess() {
            // When
            Account account = authService.register("newuser", "password123", "New User",
                    "456 New St", "new@example.com", "9876-5432", new BigDecimal("5000"));

            // Then
            assertNotNull(account);
            assertNotNull(account.id);
            assertEquals(new BigDecimal("5000.00"), account.balance.setScale(2));
            assertEquals(new BigDecimal("5000.00"), account.openBalance.setScale(2));
            assertEquals(0, account.loginCount);
            assertEquals(0, account.logoutCount);
            assertNotNull(account.creationDate);

            // Verify profile was created
            AccountProfile profile = AccountProfile.findByUserID("newuser");
            assertNotNull(profile);
            assertEquals("newuser", profile.userID);
            assertEquals("password123", profile.password);
            assertEquals("New User", profile.fullName);
            assertEquals("456 New St", profile.address);
            assertEquals("new@example.com", profile.email);
            assertEquals("9876-5432", profile.creditCard);
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is null")
        void registerNullUserID() {
            assertThrows(BadRequestException.class,
                    () -> authService.register(null, "pass", "Name", "Addr", "email", "card", BigDecimal.TEN));
        }

        @Test
        @DisplayName("should throw BadRequestException when userID is blank")
        void registerBlankUserID() {
            assertThrows(BadRequestException.class,
                    () -> authService.register("  ", "pass", "Name", "Addr", "email", "card", BigDecimal.TEN));
        }

        @Test
        @DisplayName("should throw BadRequestException when password is null")
        void registerNullPassword() {
            assertThrows(BadRequestException.class,
                    () -> authService.register("user", null, "Name", "Addr", "email", "card", BigDecimal.TEN));
        }

        @Test
        @DisplayName("should throw BadRequestException when user already exists")
        void registerDuplicateUser() {
            // Given
            authService.register("existinguser", "pass1", "User 1", "Addr", "email", "card", BigDecimal.TEN);

            // When/Then
            assertThrows(BadRequestException.class,
                    () -> authService.register("existinguser", "pass2", "User 2", "Addr", "email", "card", BigDecimal.TEN));
        }

        @Test
        @DisplayName("should handle null optional fields")
        void registerWithNullOptionalFields() {
            // When
            Account account = authService.register("user", "pass", null, null, null, null, new BigDecimal("100"));

            // Then
            assertNotNull(account);
            AccountProfile profile = AccountProfile.findByUserID("user");
            assertNull(profile.fullName);
            assertNull(profile.address);
            assertNull(profile.email);
            assertNull(profile.creditCard);
        }
    }
}

