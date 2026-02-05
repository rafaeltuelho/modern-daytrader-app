package com.daytrader.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

/**
 * Account Profile Data Transfer Object
 */
public class ProfileDTO {

    @NotBlank
    private String userId;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String address;

    @JsonProperty("creditCard")
    private String creditCard;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public ProfileDTO() {
    }

    public ProfileDTO(String userId, String fullName, String email, String address,
                     String creditCard, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.creditCard = creditCard;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        ProfileDTO that = (ProfileDTO) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(fullName, that.fullName) &&
               Objects.equals(email, that.email) &&
               Objects.equals(address, that.address) &&
               Objects.equals(creditCard, that.creditCard) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fullName, email, address, creditCard, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ProfileDTO{" +
               "userId='" + userId + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", address='" + address + '\'' +
               ", creditCard='" + (creditCard != null ? "****-****-****-" + creditCard.substring(15) : null) + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}

