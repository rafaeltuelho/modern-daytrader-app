package com.daytrader.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating user profile
 */
public class UpdateProfileRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    private String address;

    private String creditCard;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String email, String address, String creditCard) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.creditCard = creditCard;
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
}

