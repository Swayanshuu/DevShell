package com.devcli.model;

import java.time.LocalDateTime;

public class XPEvent {

    public enum EventType {
        OPEN_SOURCE_CONTRIBUTION(100, "Open source contribution"),
        PR_MERGED(50, "PR merged"),
        REPO_MAINTAINED(30, "Repository maintained"),
        TECHNICAL_LEARNING(20, "Technical learning"),
        DAILY_ACTIVITY(10, "Daily activity");

        private final int xp;
        private final String label;

        EventType(int xp, String label) {
            this.xp = xp;
            this.label = label;
        }

        public int getXp() { return xp; }
        public String getLabel() { return label; }
    }

    private String id;
    private EventType type;
    private int xpGained;
    private String description;
    private LocalDateTime timestamp;

    public XPEvent() {}

    public XPEvent(String id, EventType type, int xpGained, String description, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.xpGained = xpGained;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public int getXpGained() { return xpGained; }
    public void setXpGained(int xpGained) { this.xpGained = xpGained; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
