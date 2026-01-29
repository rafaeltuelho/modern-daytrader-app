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

import java.math.BigDecimal;
import java.util.Date;

import com.ibm.websphere.samples.daytrader.entity.Account;

/**
 * DTO for Account entity
 * Per Phase 3: Backend Migration specification section 4.2
 */
public class AccountDTO {

    private Integer accountID;
    private int loginCount;
    private int logoutCount;
    private Date lastLogin;
    private Date creationDate;
    private BigDecimal balance;
    private BigDecimal openBalance;
    private String profileID;

    public AccountDTO() {
    }

    public AccountDTO(Account account) {
        this.accountID = account.getAccountID();
        this.loginCount = account.getLoginCount();
        this.logoutCount = account.getLogoutCount();
        this.lastLogin = account.getLastLogin();
        this.creationDate = account.getCreationDate();
        this.balance = account.getBalance();
        this.openBalance = account.getOpenBalance();
        this.profileID = account.getProfile() != null ? account.getProfile().getUserID() : null;
    }

    // Getters and Setters
    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }
}

