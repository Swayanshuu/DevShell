package com.devcli.model;

import java.time.LocalDateTime;

public class Bug {
    private String id;
    private String title;
    private String project;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String status;   // OPEN, IN_PROGRESS, RESOLVED
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Bug() {}

    public Bug(String id, String title, String project, String severity, String status, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.project = project;
        this.severity = severity;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
