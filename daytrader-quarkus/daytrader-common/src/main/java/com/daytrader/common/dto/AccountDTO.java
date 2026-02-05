package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Account Data Transfer Object
 */
public class AccountDTO {

    private Long id;

    @NotNull
    @JsonProperty("profileUserId")
    private String profileUserId;

    @PositiveOrZero
    @JsonProperty("loginCount")
    private int loginCount;

    @PositiveOrZero
    @JsonProperty("logoutCount")
    private int logoutCount;

    @JsonProperty("lastLogin")
    private Instant lastLogin;

    @JsonProperty("creationDate")
    private Instant creationDate;

    @NotNull
    @PositiveOrZero
    private BigDecimal balance;

    @NotNull
    @PositiveOrZero
    @JsonProperty("openBalance")
    private BigDecimal openBalance;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public AccountDTO() {
    }

    public AccountDTO(Long id, String profileUserId, int loginCount, int logoutCount,
                     Instant lastLogin, Instant creationDate, BigDecimal balance,
                     BigDecimal openBalance, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.profileUserId = profileUserId;
        this.loginCount = loginCount;
        this.logoutCount = logoutCount;
        this.lastLogin = lastLogin;
        this.creationDate = creationDate;
        this.balance = balance;
        this.openBalance = openBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProfileUserId() {
        return profileUserId;
    }

    public void setProfileUserId(String profileUserId) {
        this.profileUserId = profileUserId;
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

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDTO that = (AccountDTO) o;
        return loginCount == that.loginCount &&
               logoutCount == that.logoutCount &&
               Objects.equals(id, that.id) &&
               Objects.equals(profileUserId, that.profileUserId) &&
               Objects.equals(lastLogin, that.lastLogin) &&
               Objects.equals(creationDate, that.creationDate) &&
               Objects.equals(balance, that.balance) &&
               Objects.equals(openBalance, that.openBalance) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, profileUserId, loginCount, logoutCount, lastLogin,
                          creationDate, balance, openBalance, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
               "id=" + id +
               ", profileUserId='" + profileUserId + '\'' +
               ", loginCount=" + loginCount +
               ", logoutCount=" + logoutCount +
               ", lastLogin=" + lastLogin +
               ", creationDate=" + creationDate +
               ", balance=" + balance +
               ", openBalance=" + openBalance +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

