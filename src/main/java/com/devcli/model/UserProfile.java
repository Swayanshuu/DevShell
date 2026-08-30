package com.devcli.model;

import java.time.LocalDateTime;

public class UserProfile {
    private long githubId;
    private String username;
    private String name;
    private String bio;
    private String avatarUrl;
    private int publicRepos;
    private int totalPrivateRepos;
    private int followers;
    private int following;
    private LocalDateTime authenticatedAt;
    private String authType; // "PAT", "OAUTH", "DEMO"
    private String token;

    public UserProfile() {}

    public UserProfile(String username, String name, String bio, String avatarUrl, int publicRepos, int totalPrivateRepos, int followers, int following, LocalDateTime authenticatedAt, String authType, String token) {
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.publicRepos = publicRepos;
        this.totalPrivateRepos = totalPrivateRepos;
        this.followers = followers;
        this.following = following;
        this.authenticatedAt = authenticatedAt;
        this.authType = authType;
        this.token = token;
    }

    public long getGithubId() { return githubId; }
    public void setGithubId(long githubId) { this.githubId = githubId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name != null && !name.isEmpty() ? name : username; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getPublicRepos() { return publicRepos; }
    public void setPublicRepos(int publicRepos) { this.publicRepos = publicRepos; }

    public int getTotalPrivateRepos() { return totalPrivateRepos; }
    public void setTotalPrivateRepos(int totalPrivateRepos) { this.totalPrivateRepos = totalPrivateRepos; }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public int getFollowing() { return following; }
    public void setFollowing(int following) { this.following = following; }

    public LocalDateTime getAuthenticatedAt() { return authenticatedAt; }
    public void setAuthenticatedAt(LocalDateTime authenticatedAt) { this.authenticatedAt = authenticatedAt; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
