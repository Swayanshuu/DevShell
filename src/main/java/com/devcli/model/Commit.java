package com.devcli.model;

import java.time.LocalDateTime;

public class Commit {
    private String sha;
    private String repoName;
    private String message;
    private String author;
    private String authorEmail;
    private LocalDateTime date;
    private String url;

    public Commit() {}

    public Commit(String sha, String repoName, String message, String author, String authorEmail, LocalDateTime date, String url) {
        this.sha = sha;
        this.repoName = repoName;
        this.message = message;
        this.author = author;
        this.authorEmail = authorEmail;
        this.date = date;
        this.url = url;
    }

    public String getSha() { return sha; }
    public void setSha(String sha) { this.sha = sha; }

    public String getShortSha() { return sha != null && sha.length() >= 7 ? sha.substring(0, 7) : sha; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getShortMessage() {
        if (message == null) return "";
        int firstLineEnd = message.indexOf('\n');
        String firstLine = firstLineEnd > 0 ? message.substring(0, firstLineEnd) : message;
        return firstLine.length() > 60 ? firstLine.substring(0, 57) + "..." : firstLine;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
