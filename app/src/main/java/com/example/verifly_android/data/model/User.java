package com.example.verifly_android.data.model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private boolean isVerified;
    private long createdAt;

    public User() {
    }

    public User(String id, String fullName, String email, String phoneNumber, String password, boolean isVerified, long createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
    }

    public User(String fullName, String email, String phoneNumber, String password) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isVerified = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) ||
                (Objects.equals(email, user.email) && Objects.equals(phoneNumber, user.phoneNumber));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, phoneNumber);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isVerified=" + isVerified +
                ", createdAt=" + createdAt +
                '}';
    }
}
