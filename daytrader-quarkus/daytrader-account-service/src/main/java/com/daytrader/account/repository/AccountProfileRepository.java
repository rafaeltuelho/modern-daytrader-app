package com.daytrader.account.repository;

import com.daytrader.account.entity.AccountProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repository for AccountProfile entity
 */
@ApplicationScoped
public class AccountProfileRepository implements PanacheRepository<AccountProfile> {

    /**
     * Find profile by user ID
     */
    public Optional<AccountProfile> findByUserId(String userId) {
        return find("userId", userId).firstResultOptional();
    }

    /**
     * Find profile by email
     */
    public Optional<AccountProfile> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Check if user ID exists
     */
    public boolean existsByUserId(String userId) {
        return count("userId", userId) > 0;
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}

