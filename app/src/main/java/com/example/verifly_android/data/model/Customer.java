package com.example.verifly_android.data.model;

import java.io.Serializable;
import java.util.UUID;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String status;
    private String notes;
    private long createdAt;

    public Customer() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.status = "Active";
    }

    public Customer(String id, String name, String email, String phone, String address, String status, String notes, long createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status != null ? status : "Active";
        this.notes = notes;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public Customer(String name, String email, String phone, String address, String notes) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = "Active";
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
