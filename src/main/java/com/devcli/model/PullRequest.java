package com.devcli.model;

import java.time.LocalDateTime;

public class PullRequest {
    private long id;
    private String repoName;
    private int number;
    private String title;
    private String state; // OPEN, CLOSED, MERGED
    private LocalDateTime createdAt;
    private LocalDateTime mergedAt;
    private String url;
    private String author;

    public PullRequest() {}

    public PullRequest(long id, String repoName, int number, String title, String state, LocalDateTime createdAt, LocalDateTime mergedAt, String url, String author) {
        this.id = id;
        this.repoName = repoName;
        this.number = number;
        this.title = title;
        this.state = state;
        this.createdAt = createdAt;
        this.mergedAt = mergedAt;
        this.url = url;
        this.author = author;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getMergedAt() { return mergedAt; }
    public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
