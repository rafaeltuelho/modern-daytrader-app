package com.daytrader.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Login Request DTO
 */
public class LoginRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String userId;

    @NotBlank
    @Size(min = 6)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginRequest that = (LoginRequest) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, password);
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
               "userId='" + userId + '\'' +
               ", password='[PROTECTED]'" +
               '}';
    }
}

