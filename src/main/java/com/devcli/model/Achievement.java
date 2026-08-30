package com.devcli.model;

import java.time.LocalDateTime;

public class Achievement {
    private String id;
    private String code;
    private String title;
    private String icon;
    private String description;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private String progressText;

    public Achievement() {}

    public Achievement(String id, String code, String title, String icon, String description, boolean unlocked, LocalDateTime unlockedAt, String progressText) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
        this.progressText = progressText;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }

    public String getProgressText() { return progressText; }
    public void setProgressText(String progressText) { this.progressText = progressText; }
}
