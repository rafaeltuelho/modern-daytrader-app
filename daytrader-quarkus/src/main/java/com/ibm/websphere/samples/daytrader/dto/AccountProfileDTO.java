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
package com.ibm.websphere.samples.daytrader.dto;

import com.ibm.websphere.samples.daytrader.entity.AccountProfile;

/**
 * DTO for AccountProfile entity
 * Per Phase 3: Backend Migration specification section 4.2
 */
public class AccountProfileDTO {

    private String userID;
    private String password;
    private String fullName;
    private String address;
    private String email;
    private String creditCard;

    public AccountProfileDTO() {
    }

    public AccountProfileDTO(AccountProfile profile) {
        this.userID = profile.getUserID();
        this.password = profile.getPassword();
        this.fullName = profile.getFullName();
        this.address = profile.getAddress();
        this.email = profile.getEmail();
        this.creditCard = profile.getCreditCard();
    }

    // Getters and Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }
}

