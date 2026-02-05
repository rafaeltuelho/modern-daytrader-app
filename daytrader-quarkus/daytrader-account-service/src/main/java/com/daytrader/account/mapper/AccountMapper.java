package com.daytrader.account.mapper;

import com.daytrader.account.entity.Account;
import com.daytrader.account.entity.AccountProfile;
import com.daytrader.common.dto.AccountDTO;
import com.daytrader.common.dto.AccountResponse;
import com.daytrader.common.dto.ProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Account and AccountProfile entities
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface AccountMapper {

    /**
     * Map Account entity to AccountDTO
     *
     * NOTE: Implemented as default method to avoid MapStruct bug with maskCreditCard
     */
    default AccountDTO toAccountDTO(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDTO(
            account.id,
            account.profile != null ? account.profile.userId : null,
            account.loginCount,
            account.logoutCount,
            account.lastLogin,
            account.creationDate,
            account.balance,
            account.openBalance,
            account.createdAt,
            account.updatedAt
        );
    }

    /**
     * Map AccountProfile entity to ProfileDTO
     * Masks credit card number for security
     *
     * NOTE: We implement this as a default method instead of using @Mapping with expression
     * to avoid MapStruct bug where it applies the expression to all fields
     */
    default ProfileDTO toProfileDTO(AccountProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileDTO(
            profile.userId,
            profile.fullName,
            profile.email,
            profile.address,
            maskCreditCard(profile.creditCard),
            profile.createdAt,
            profile.updatedAt
        );
    }

    /**
     * Map Account entity to AccountResponse (includes profile)
     *
     * NOTE: Implemented as default method to avoid MapStruct bug with maskCreditCard
     */
    default AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountResponse(
            account.id,
            account.profile != null ? account.profile.userId : null,
            account.balance,
            account.openBalance,
            account.loginCount,
            account.logoutCount,
            account.lastLogin,
            account.creationDate,
            accountToProfileDTO(account)
        );
    }

    /**
     * Helper method to map Account to ProfileDTO for nested mapping
     */
    default ProfileDTO accountToProfileDTO(Account account) {
        if (account == null || account.profile == null) {
            return null;
        }
        return toProfileDTO(account.profile);
    }

    /**
     * Mask credit card number - show only last 4 digits
     */
    default String maskCreditCard(String creditCard) {
        if (creditCard == null || creditCard.length() < 4) {
            return "****-****-****-****";
        }
        String lastFour = creditCard.substring(creditCard.length() - 4);
        return "****-****-****-" + lastFour;
    }
}

