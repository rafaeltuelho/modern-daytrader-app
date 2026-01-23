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

import com.ibm.websphere.samples.daytrader.entities.Account;
import com.ibm.websphere.samples.daytrader.entities.AccountProfile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Account service for retrieving and updating account data.
 */
@ApplicationScoped
public class AccountService {

    /**
     * Get account data for a user.
     *
     * @param userID the account userID to lookup
     * @return Account data, or null if not found
     */
    public Account getAccount(String userID) {
        if (userID == null || userID.isBlank()) {
            return null;
        }
        return Account.findByProfileUserID(userID);
    }

    /**
     * Get account data for a user (alias for getAccount).
     *
     * @param userID the account userID to lookup
     * @return Account data
     * @throws NotFoundException if account doesn't exist
     */
    public Account getAccountData(String userID) {
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }

        Account account = Account.findByProfileUserID(userID);
        if (account == null) {
            throw new NotFoundException("Account not found for user: " + userID);
        }

        return account;
    }

    /**
     * Get account profile data for a user.
     *
     * @param userID the account userID to lookup
     * @return AccountProfile data, or null if not found
     */
    public AccountProfile getAccountProfile(String userID) {
        if (userID == null || userID.isBlank()) {
            return null;
        }
        return AccountProfile.findByUserID(userID);
    }

    /**
     * Get account profile data for a user (alias for getAccountProfile).
     *
     * @param userID the account userID to lookup
     * @return AccountProfile data
     * @throws NotFoundException if profile doesn't exist
     */
    public AccountProfile getAccountProfileData(String userID) {
        if (userID == null || userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }

        AccountProfile profile = AccountProfile.findByUserID(userID);
        if (profile == null) {
            throw new NotFoundException("Profile not found for user: " + userID);
        }

        return profile;
    }

    /**
     * Update account profile information with individual fields.
     *
     * @param userID     the user ID
     * @param password   new password (optional)
     * @param fullName   new full name (optional)
     * @param address    new address (optional)
     * @param email      new email (optional)
     * @param creditCard new credit card (optional)
     * @return the updated AccountProfile, or null if not found
     */
    @Transactional
    public AccountProfile updateAccountProfile(String userID, String password,
                                               String fullName, String address,
                                               String email, String creditCard) {
        if (userID == null || userID.isBlank()) {
            return null;
        }

        AccountProfile existingProfile = AccountProfile.findByUserID(userID);
        if (existingProfile == null) {
            return null;
        }

        // Update fields if provided
        if (password != null) existingProfile.password = password;
        if (fullName != null) existingProfile.fullName = fullName;
        if (address != null) existingProfile.address = address;
        if (email != null) existingProfile.email = email;
        if (creditCard != null) existingProfile.creditCard = creditCard;

        return existingProfile;
    }

    /**
     * Update account profile information.
     *
     * @param profile the profile data to update
     * @return the updated AccountProfile
     * @throws NotFoundException if profile doesn't exist
     * @throws BadRequestException if profile is null or userID is missing
     */
    @Transactional
    public AccountProfile updateAccountProfile(AccountProfile profile) {
        if (profile == null) {
            throw new BadRequestException("Profile data is required");
        }
        if (profile.userID == null || profile.userID.isBlank()) {
            throw new BadRequestException("User ID is required");
        }

        AccountProfile existingProfile = AccountProfile.findByUserID(profile.userID);
        if (existingProfile == null) {
            throw new NotFoundException("Profile not found for user: " + profile.userID);
        }

        // Update fields
        existingProfile.password = profile.password;
        existingProfile.fullName = profile.fullName;
        existingProfile.address = profile.address;
        existingProfile.email = profile.email;
        existingProfile.creditCard = profile.creditCard;

        return existingProfile;
    }
}

