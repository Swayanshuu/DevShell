package com.devcli.model;

import java.time.LocalDateTime;

public class ActivityEvent {
    private String id;
    private String type; // PushEvent, PullRequestEvent, IssuesEvent, CreateEvent, WatchEvent
    private String repoName;
    private String description;
    private LocalDateTime timestamp;

    public ActivityEvent() {}

    public ActivityEvent(String id, String type, String repoName, String description, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.repoName = repoName;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
