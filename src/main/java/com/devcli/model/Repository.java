package com.devcli.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Repository {

    public enum Status {
        ACTIVE("🟢 Active"),
        RECENTLY_ACTIVE("🟡 Recently Active"),
        INACTIVE("⚪ Inactive"),
        ARCHIVED("📦 Archived");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String name;
    private String fullName;
    private String owner;
    private String description;
    private String language;
    private int stars;
    private int forks;
    private boolean isPrivate;
    private boolean isArchived;
    private String defaultBranch;
    private LocalDateTime updatedAt;
    private LocalDateTime lastCommitAt;
    private Status status;
    private int commitCount;
    private Map<String, Long> languages = new HashMap<>();

    public Repository() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language != null ? language : "Unknown"; }
    public void setLanguage(String language) { this.language = language; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public int getForks() { return forks; }
    public void setForks(int forks) { this.forks = forks; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastCommitAt() { return lastCommitAt; }
    public void setLastCommitAt(LocalDateTime lastCommitAt) { this.lastCommitAt = lastCommitAt; }

    public Status getStatus() { return status != null ? status : Status.INACTIVE; }
    public void setStatus(Status status) { this.status = status; }

    public int getCommitCount() { return commitCount; }
    public void setCommitCount(int commitCount) { this.commitCount = commitCount; }

    public Map<String, Long> getLanguages() { return languages; }
    public void setLanguages(Map<String, Long> languages) { this.languages = languages; }
}
