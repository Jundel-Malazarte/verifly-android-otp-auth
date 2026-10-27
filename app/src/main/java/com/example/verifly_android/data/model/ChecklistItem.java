package com.example.verifly_android.data.model;

import java.io.Serializable;
import java.util.UUID;

public class ChecklistItem implements Serializable {
    private String id;
    private String customerId;
    private String title;
    private String category;
    private boolean isCompleted;
    private long createdAt;

    public ChecklistItem() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    public ChecklistItem(String id, String customerId, String title, String category, boolean isCompleted, long createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.customerId = customerId;
        this.title = title;
        this.category = category != null ? category : "General";
        this.isCompleted = isCompleted;
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public ChecklistItem(String title, String category) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.isCompleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
