package com.devcli.model;

import java.time.LocalDateTime;

public class ProjectHealth {
    public enum State {
        ACTIVE("ACTIVE", "🟢 High recent activity & active delivery"),
        MAINTAINED("MAINTAINED", "🟡 Stable code base & maintained commits"),
        LOW_ACTIVITY("LOW ACTIVITY", "⚪ Minimal recent commits"),
        DORMANT("DORMANT", "📦 No commits in the last 90+ days");

        private final String code;
        private final String description;

        State(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    private String repoName;
    private int commitCount;
    private int contributorCount;
    private int openIssuesCount;
    private int pullRequestCount;
    private LocalDateTime lastActivity;
    private State state;

    public ProjectHealth() {}

    public ProjectHealth(String repoName, int commitCount, int contributorCount, int openIssuesCount, int pullRequestCount, LocalDateTime lastActivity, State state) {
        this.repoName = repoName;
        this.commitCount = commitCount;
        this.contributorCount = contributorCount;
        this.openIssuesCount = openIssuesCount;
        this.pullRequestCount = pullRequestCount;
        this.lastActivity = lastActivity;
        this.state = state;
    }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public int getCommitCount() { return commitCount; }
    public void setCommitCount(int commitCount) { this.commitCount = commitCount; }

    public int getContributorCount() { return contributorCount; }
    public void setContributorCount(int contributorCount) { this.contributorCount = contributorCount; }

    public int getOpenIssuesCount() { return openIssuesCount; }
    public void setOpenIssuesCount(int openIssuesCount) { this.openIssuesCount = openIssuesCount; }

    public int getPullRequestCount() { return pullRequestCount; }
    public void setPullRequestCount(int pullRequestCount) { this.pullRequestCount = pullRequestCount; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public State getState() { return state != null ? state : State.MAINTAINED; }
    public void setState(State state) { this.state = state; }
}
