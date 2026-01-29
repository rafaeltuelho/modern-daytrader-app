/**
 * (C) Copyright IBM Corporation 2015, 2024.
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

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
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

/**
 * Account entity - represents a brokerage account
 * Migrated from AccountDataBean per Phase 3: Backend Migration specification
 */
@Entity
@Table(name = "accountejb")
public class Account implements Serializable {

    private static final long serialVersionUID = 8437841265136840545L;

    @TableGenerator(
        name = "accountIdGen", 
        table = "KEYGENEJB", 
        pkColumnName = "KEYNAME", 
        valueColumnName = "KEYVAL", 
        pkColumnValue = "account", 
        allocationSize = 1000
    )
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "accountIdGen")
    @Column(name = "ACCOUNTID", nullable = false)
    private Integer accountID;

    @NotNull
    @Column(name = "LOGINCOUNT", nullable = false)
    private int loginCount;

    @NotNull
    @Column(name = "LOGOUTCOUNT", nullable = false)
    private int logoutCount;

    @Column(name = "LASTLOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Column(name = "CREATIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "OPENBALANCE")
    private BigDecimal openBalance;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Order> orders;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Collection<Holding> holdings;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_USERID")
    private AccountProfile profile;

    @Transient
    private String profileID;

    public Account() {
    }

    public Account(Integer accountID, int loginCount, int logoutCount, Date lastLogin, 
                   Date creationDate, BigDecimal balance, BigDecimal openBalance, String profileID) {
        this.accountID = accountID;
        this.loginCount = loginCount;
        this.logoutCount = logoutCount;
        this.lastLogin = lastLogin;
        this.creationDate = creationDate;
        this.balance = balance;
        this.openBalance = openBalance;
        this.profileID = profileID;
    }

    public void login(String password) {
        if (profile == null || !profile.getPassword().equals(password)) {
            throw new IllegalArgumentException("Login failure for account: " + accountID);
        }
        this.lastLogin = new Timestamp(System.currentTimeMillis());
        this.loginCount++;
    }

    public void logout() {
        this.logoutCount++;
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

    public Collection<Order> getOrders() {
        return orders;
    }

    public void setOrders(Collection<Order> orders) {
        this.orders = orders;
    }

    public Collection<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(Collection<Holding> holdings) {
        this.holdings = holdings;
    }

    public AccountProfile getProfile() {
        return profile;
    }

    public void setProfile(AccountProfile profile) {
        this.profile = profile;
    }

    @Override
    public int hashCode() {
        return accountID != null ? accountID.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Account)) return false;
        Account other = (Account) obj;
        return accountID != null && accountID.equals(other.accountID);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountID=" + accountID +
                ", loginCount=" + loginCount +
                ", logoutCount=" + logoutCount +
                ", lastLogin=" + lastLogin +
                ", creationDate=" + creationDate +
                ", balance=" + balance +
                ", openBalance=" + openBalance +
                ", profileID='" + profileID + '\'' +
                '}';
    }
}

