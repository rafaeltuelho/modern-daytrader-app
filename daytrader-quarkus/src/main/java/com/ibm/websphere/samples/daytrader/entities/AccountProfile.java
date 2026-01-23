/**
 * (C) Copyright IBM Corporation 2015, 2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "accountprofileejb")
public class AccountProfile extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "USERID", nullable = false)
    public String userID;

    @Column(name = "PASSWD")
    public String password;

    @Column(name = "FULLNAME")
    public String fullName;

    @Column(name = "ADDRESS")
    public String address;

    @Column(name = "EMAIL")
    public String email;

    @Column(name = "CREDITCARD")
    public String creditCard;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    @JsonIgnore
    public Account account;

    public AccountProfile() {
    }

    public AccountProfile(String userID, String password, String fullName, 
                         String address, String email, String creditCard) {
        this.userID = userID;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.creditCard = creditCard;
    }

    // Panache finder methods
    public static AccountProfile findByUserID(String userID) {
        return findById(userID);
    }

    // Getters and setters for compatibility
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "AccountProfile{" +
                "userID='" + userID + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

