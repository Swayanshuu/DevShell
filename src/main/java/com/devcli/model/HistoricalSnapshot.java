package com.devcli.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HistoricalSnapshot {
    private LocalDate date;
    private int commitCount;
    private int pullRequestCount;
    private int reviewCount;
    private int activeRepoCount;
    private int additions;
    private int deletions;
    private Map<String, Long> topLanguages = new HashMap<>();

    public HistoricalSnapshot() {}

    public HistoricalSnapshot(LocalDate date, int commitCount, int pullRequestCount, int reviewCount, int activeRepoCount, int additions, int deletions) {
        this.date = date;
        this.commitCount = commitCount;
        this.pullRequestCount = pullRequestCount;
        this.reviewCount = reviewCount;
        this.activeRepoCount = activeRepoCount;
        this.additions = additions;
        this.deletions = deletions;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getCommitCount() { return commitCount; }
    public void setCommitCount(int commitCount) { this.commitCount = commitCount; }

    public int getPullRequestCount() { return pullRequestCount; }
    public void setPullRequestCount(int pullRequestCount) { this.pullRequestCount = pullRequestCount; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getActiveRepoCount() { return activeRepoCount; }
    public void setActiveRepoCount(int activeRepoCount) { this.activeRepoCount = activeRepoCount; }

    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }

    public Map<String, Long> getTopLanguages() { return topLanguages; }
    public void setTopLanguages(Map<String, Long> topLanguages) { this.topLanguages = topLanguages; }
}
