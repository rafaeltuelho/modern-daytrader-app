package com.daytrader.account.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account Entity - Trading account information
 * Maps to account table
 */
@Entity
@Table(name = "account")
public class Account extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_id_seq", allocationSize = 50)
    public Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    public AccountProfile profile;

    @Column(name = "login_count", nullable = false)
    public int loginCount = 0;

    @Column(name = "logout_count", nullable = false)
    public int logoutCount = 0;

    @Column(name = "last_login")
    public Instant lastLogin;

    @Column(name = "creation_date", updatable = false)
    public Instant creationDate;

    @Column(precision = 14, scale = 2, nullable = false)
    public BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "open_balance", precision = 14, scale = 2, nullable = false)
    public BigDecimal openBalance = BigDecimal.ZERO;

    @Version
    public int version;

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (creationDate == null) {
            creationDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Find account by profile user ID
     */
    public static Account findByProfileUserId(String userId) {
        return find("profile.userId", userId).firstResult();
    }

    /**
     * Find account by ID
     */
    public static Account findByAccountId(Long accountId) {
        return findById(accountId);
    }

    /**
     * Check if account exists for user
     */
    public static boolean existsByProfileUserId(String userId) {
        return count("profile.userId", userId) > 0;
    }
}

