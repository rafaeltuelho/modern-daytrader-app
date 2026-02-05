package com.daytrader.account.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Account Profile Entity - User profile information
 * Maps to account_profile table
 */
@Entity
@Table(name = "account_profile")
public class AccountProfile extends PanacheEntityBase {

    @Id
    @Column(name = "user_id", length = 255)
    public String userId;

    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;

    @Column(name = "full_name", length = 255)
    public String fullName;

    @Column(length = 500)
    public String address;

    @Column(length = 255)
    public String email;

    @Column(name = "credit_card", length = 255)
    public String creditCard;

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
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Find profile by user ID
     */
    public static AccountProfile findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }

    /**
     * Find profile by email
     */
    public static AccountProfile findByEmail(String email) {
        return find("email", email).firstResult();
    }

    /**
     * Check if user ID exists
     */
    public static boolean existsByUserId(String userId) {
        return count("userId", userId) > 0;
    }

    /**
     * Check if email exists
     */
    public static boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}

