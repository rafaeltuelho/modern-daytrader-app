package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Registration Request DTO
 */
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_:-]+$")
    private String userId;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Size(max = 200)
    private String address;

    @Pattern(regexp = "^$|^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$", message = "Credit card must be in format XXXX-XXXX-XXXX-XXXX")
    private String creditCard;

    @PositiveOrZero
    @DecimalMax("1000000")
    @JsonProperty("openBalance")
    private BigDecimal openBalance;

    public RegisterRequest() {
    }

    public RegisterRequest(String userId, String password, String fullName, String email,
                          String address, String creditCard, BigDecimal openBalance) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.creditCard = creditCard;
        this.openBalance = openBalance != null ? openBalance : new BigDecimal("10000.00");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterRequest that = (RegisterRequest) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(password, that.password) &&
               Objects.equals(fullName, that.fullName) &&
               Objects.equals(email, that.email) &&
               Objects.equals(address, that.address) &&
               Objects.equals(creditCard, that.creditCard) &&
               Objects.equals(openBalance, that.openBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, password, fullName, email, address, creditCard, openBalance);
    }

    @Override
    public String toString() {
        String maskedCreditCard = null;
        if (creditCard != null && creditCard.length() >= 19) {
            maskedCreditCard = "****-****-****-" + creditCard.substring(15);
        } else if (creditCard != null && !creditCard.isEmpty()) {
            maskedCreditCard = "****";
        }
        return "RegisterRequest{" +
               "userId='" + userId + '\'' +
               ", password='[PROTECTED]'" +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", address='" + address + '\'' +
               ", creditCard='" + maskedCreditCard + '\'' +
               ", openBalance=" + openBalance +
               '}';
    }
}

