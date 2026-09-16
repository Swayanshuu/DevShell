package com.devcli.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Learning {
    private String id;
    private String title;
    
    @JsonAlias({"description", "note"})
    private String content;
    
    private String category;
    private List<String> tags = new ArrayList<>();
    private String project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean pinned;

    public Learning() {}

    public Learning(String id, String title, String category, String content, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.category = category != null ? category : "General";
        this.content = content != null ? content : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.project = "General";
        this.pinned = false;
        this.tags = new ArrayList<>();
    }

    public Learning(String id, String title, String content, String category, List<String> tags, String project, LocalDateTime createdAt, LocalDateTime updatedAt, boolean pinned) {
        this.id = id;
        this.title = title;
        this.content = content != null ? content : "";
        this.category = category != null ? category : "General";
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.project = project != null ? project : "General";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.pinned = pinned;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // Backward compatibility for legacy code/JSON
    public String getDescription() { return content; }
    public void setDescription(String description) { this.content = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public String getProject() { return project != null ? project : "General"; }
    public void setProject(String project) { this.project = project != null ? project : "General"; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt != null ? updatedAt : createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
}
