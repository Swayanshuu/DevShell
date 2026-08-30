package com.devcli.model;

import java.time.LocalDateTime;

public class Learning {
    private String id;
    private String title;
    private String category;
    private String description;
    private LocalDateTime createdAt;

    public Learning() {}

    public Learning(String id, String title, String category, String description, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
