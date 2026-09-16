package com.devcli.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Goal {

    public enum Status {
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        ABANDONED("Abandoned");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String id;
    private String title;
    private String description;
    private int targetValue;
    private int currentValue;
    private Status status;
    private LocalDateTime createdDate;
    private LocalDate targetDate;

    public Goal() {}

    public Goal(String id, String title, String description, int targetValue, int currentValue, Status status, LocalDateTime createdDate, LocalDate targetDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.status = status;
        this.createdDate = createdDate;
        this.targetDate = targetDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public int getCurrentValue() { return currentValue; }
    public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }

    public Status getStatus() { return status != null ? status : Status.IN_PROGRESS; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public int getProgressPercentage() {
        if (targetValue <= 0) return currentValue >= 1 ? 100 : 0;
        int pct = (int) Math.round(((double) currentValue / targetValue) * 100);
        return Math.min(100, Math.max(0, pct));
    }
}
