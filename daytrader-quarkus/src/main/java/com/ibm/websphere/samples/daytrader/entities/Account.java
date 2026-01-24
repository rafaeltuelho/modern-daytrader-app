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
package com.ibm.websphere.samples.daytrader.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "accountejb")
public class Account extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "LOGINCOUNT", nullable = false)
    public int loginCount;

    @NotNull
    @Column(name = "LOGOUTCOUNT", nullable = false)
    public int logoutCount;

    @Column(name = "LASTLOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    @Column(name = "CREATIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date creationDate;

    @Column(name = "BALANCE")
    public BigDecimal balance;

    @Column(name = "OPENBALANCE")
    public BigDecimal openBalance;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Order> orders;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Holding> holdings;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_USERID")
    public AccountProfile profile;

    public Account() {
    }

    public Account(int loginCount, int logoutCount, Date lastLogin, Date creationDate,
                   BigDecimal balance, BigDecimal openBalance) {
        this.loginCount = loginCount;
        this.logoutCount = logoutCount;
        this.lastLogin = lastLogin;
        this.creationDate = creationDate;
        this.balance = balance;
        this.openBalance = openBalance;
    }

    // Panache finder methods
    public static Account findByProfileUserID(String userID) {
        return find("profile.userID", userID).firstResult();
    }

    public static List<Account> findAllAccounts() {
        return listAll();
    }

    // Business methods
    public void login(String password) {
        if (profile == null || !profile.password.equals(password)) {
            throw new SecurityException("Login failure for account: " + id);
        }
        lastLogin = new Timestamp(System.currentTimeMillis());
        loginCount++;
    }

    public void logout() {
        logoutCount++;
    }

    // Getters and setters for compatibility
    public Long getAccountID() {
        return id;
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

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public AccountProfile getProfile() {
        return profile;
    }

    public void setProfile(AccountProfile profile) {
        this.profile = profile;
    }

    public String getProfileID() {
        return profile != null ? profile.userID : null;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", loginCount=" + loginCount +
                ", logoutCount=" + logoutCount +
                ", balance=" + balance +
                ", openBalance=" + openBalance +
                '}';
    }
}

