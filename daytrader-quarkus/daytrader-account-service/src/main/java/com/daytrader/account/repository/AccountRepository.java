package com.daytrader.account.repository;

import com.daytrader.account.entity.Account;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repository for Account entity
 */
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    /**
     * Find account by profile user ID
     */
    public Optional<Account> findByProfileUserId(String userId) {
        return find("profile.userId", userId).firstResultOptional();
    }

    /**
     * Find account by ID
     */
    public Optional<Account> findByAccountId(Long accountId) {
        return findByIdOptional(accountId);
    }

    /**
     * Check if account exists for user
     */
    public boolean existsByProfileUserId(String userId) {
        return count("profile.userId", userId) > 0;
    }
}

