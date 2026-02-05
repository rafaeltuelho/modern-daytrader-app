package com.daytrader.account.service;

import com.daytrader.account.entity.Account;
import com.daytrader.account.entity.AccountProfile;
import com.daytrader.account.mapper.AccountMapper;
import com.daytrader.account.repository.AccountProfileRepository;
import com.daytrader.account.repository.AccountRepository;
import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.ChangePasswordRequest;
import com.daytrader.common.dto.ProfileDTO;
import com.daytrader.common.dto.RegisterRequest;
import com.daytrader.common.dto.UpdateProfileRequest;
import com.daytrader.common.exception.BusinessException;
import com.daytrader.common.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Account Service - Business logic for account operations
 */
@ApplicationScoped
public class AccountService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountProfileRepository profileRepository;

    @Inject
    AccountMapper accountMapper;

    /**
     * Get account by account ID
     */
    public AccountResponse getAccount(Long accountId) {
        LOG.debugf("Getting account by ID: %d", accountId);
        Account account = accountRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return accountMapper.toAccountResponse(account);
    }

    /**
     * Get account by user ID
     */
    public AccountResponse getAccountByUserId(String userId) {
        LOG.debugf("Getting account by user ID: %s", userId);
        Account account = accountRepository.findByProfileUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        return accountMapper.toAccountResponse(account);
    }

    /**
     * Get profile by user ID
     */
    public ProfileDTO getProfile(String userId) {
        LOG.debugf("Getting profile for user: %s", userId);
        AccountProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
        return accountMapper.toProfileDTO(profile);
    }

    /**
     * Validate user credentials for login.
     * Returns the user's profile if credentials are valid, null otherwise.
     */
    public ProfileDTO validateCredentials(String userId, String password) {
        LOG.debugf("Validating credentials for user: %s", userId);

        return profileRepository.findByUserId(userId)
            .filter(profile -> verifyPassword(password, profile.passwordHash))
            .map(accountMapper::toProfileDTO)
            .orElse(null);
    }

    /**
     * Verify password against stored hash.
     * TODO: Implement proper BCrypt verification in production.
     */
    private boolean verifyPassword(String password, String storedHash) {
        // For development: accept any non-empty password
        // In production, use BCrypt.checkpw(password, storedHash)
        return password != null && !password.isEmpty();
    }

    /**
     * Register a new account
     */
    @Transactional
    public AccountResponse register(RegisterRequest request) {
        LOG.infof("Registering new account for user: %s", request.getUserId());

        // Check if user already exists
        if (profileRepository.existsByUserId(request.getUserId())) {
            throw new BusinessException("USER_EXISTS", "User ID '" + request.getUserId() + "' is already registered");
        }

        // Check if email already exists
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email '" + request.getEmail() + "' is already registered");
        }

        // Create profile
        AccountProfile profile = new AccountProfile();
        profile.userId = request.getUserId();
        profile.passwordHash = hashPassword(request.getPassword()); // TODO: Implement proper password hashing
        profile.fullName = request.getFullName();
        profile.email = request.getEmail();
        profile.address = request.getAddress();
        profile.creditCard = request.getCreditCard();
        profileRepository.persist(profile);

        // Create account
        Account account = new Account();
        account.profile = profile;
        account.balance = request.getOpenBalance();
        account.openBalance = request.getOpenBalance();
        account.loginCount = 0;
        account.logoutCount = 0;
        accountRepository.persist(account);

        LOG.infof("Successfully registered account ID %d for user %s", account.id, request.getUserId());
        return accountMapper.toAccountResponse(account);
    }

    /**
     * Record login
     */
    @Transactional
    public void recordLogin(String userId) {
        LOG.debugf("Recording login for user: %s", userId);
        Account account = accountRepository.findByProfileUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        account.loginCount++;
        account.lastLogin = Instant.now();
        accountRepository.persist(account);
    }

    /**
     * Record logout
     */
    @Transactional
    public void recordLogout(String userId) {
        LOG.debugf("Recording logout for user: %s", userId);
        Account account = accountRepository.findByProfileUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        account.logoutCount++;
        accountRepository.persist(account);
    }

    /**
     * Update account balance
     */
    @Transactional
    public void updateBalance(Long accountId, BigDecimal newBalance) {
        LOG.debugf("Updating balance for account %d to %s", accountId, newBalance);
        Account account = accountRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        
        account.balance = newBalance;
        accountRepository.persist(account);
    }

    /**
     * Update user profile
     */
    @Transactional
    public ProfileDTO updateProfile(String userId, UpdateProfileRequest request) {
        LOG.debugf("Updating profile for user: %s", userId);
        AccountProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        // Check if email is being changed and if it's already in use by another user
        if (!profile.email.equals(request.getEmail()) &&
            profileRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email '" + request.getEmail() + "' is already in use");
        }

        // Update profile fields
        profile.fullName = request.getFullName();
        profile.email = request.getEmail();
        profile.address = request.getAddress();
        profile.creditCard = request.getCreditCard();

        profileRepository.persist(profile);

        LOG.infof("Successfully updated profile for user: %s", userId);
        return accountMapper.toProfileDTO(profile);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        LOG.debugf("Changing password for user: %s", userId);
        AccountProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        // Verify current password
        if (!verifyPassword(request.getCurrentPassword(), profile.passwordHash)) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }

        // Update password
        profile.passwordHash = hashPassword(request.getNewPassword());
        profileRepository.persist(profile);

        LOG.infof("Successfully changed password for user: %s", userId);
    }

    /**
     * Temporary password hashing - should use BCrypt in production
     */
    private String hashPassword(String password) {
        // TODO: Implement proper BCrypt password hashing
        // For now, return a placeholder hash
        return "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
    }
}

