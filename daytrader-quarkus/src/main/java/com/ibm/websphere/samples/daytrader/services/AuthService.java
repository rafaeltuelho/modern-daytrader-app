/**
 * (C) Copyright IBM Corporation 2015, 2025.
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
package com.ibm.websphere.samples.daytrader.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;

/**
 * Authentication service for user login, logout, and registration operations.
 */
@ApplicationScoped
public class AuthService {

    /**
     * Authenticate and login a user with the given password.
     *
     * @param userID   the customer to login
     * @param password the password for authentication
     * @return Account data for the logged-in user
     * @throws NotFoundException if user doesn't exist
     * @throws NotAuthorizedException if password is invalid
     */
    @Transactional
    public Account login(String userID, String password) {
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }

        AccountProfile profile = AccountProfile.findByUserID(userID);
        if (profile == null) {
            throw new NotFoundException("User not found: " + userID);
        }

        if (!password.equals(profile.password)) {
            throw new NotAuthorizedException("Invalid password for user: " + userID);
        }

        Account account = Account.findByProfileUserID(userID);
        if (account == null) {
            throw new NotFoundException("Account not found for user: " + userID);
        }

        // Update login statistics
        account.loginCount++;
        account.lastLogin = new Timestamp(System.currentTimeMillis());

        return account;
    }

    /**
     * Logout the given user by incrementing the logout count.
     *
     * @param userID the customer to logout
     * @throws NotFoundException if user doesn't exist
     */
    @Transactional
    public void logout(String userID) {
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }

        Account account = Account.findByProfileUserID(userID);
        if (account == null) {
            throw new NotFoundException("Account not found for user: " + userID);
        }

        account.logoutCount++;
    }

    /**
     * Register a new Trade customer. Creates a new user profile and account.
     *
     * @param userID      the new customer to register
     * @param password    the customer's password
     * @param fullname    the customer's full name
     * @param address     the customer's address
     * @param email       the customer's email
     * @param creditCard  the customer's credit card number
     * @param openBalance the initial account balance
     * @return the newly created Account
     * @throws BadRequestException if user already exists or required fields are missing
     */
    @Transactional
    public Account register(String userID, String password, String fullname,
                           String address, String email, String creditCard,
                           BigDecimal openBalance) {
        // Validate required fields
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }

        // Check if user already exists
        AccountProfile existingProfile = AccountProfile.findByUserID(userID);
        if (existingProfile != null) {
            throw new BadRequestException("User already exists: " + userID);
        }

        // Create new profile
        AccountProfile profile = new AccountProfile(userID, password, fullname, 
                                                   address, email, creditCard);
        profile.persist();

        // Create new account with initial balance
        BigDecimal balance = openBalance != null ? openBalance : BigDecimal.ZERO;
        Account account = new Account(0, 0, new Timestamp(System.currentTimeMillis()),
                                     new Date(), balance, balance);
        account.profile = profile;
        account.persist();

        return account;
    }
}

