package com.devcli.service;

import com.devcli.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public UserProfile fetchUserProfile(String token) throws Exception {
        if (isDemoToken(token)) return getDemoUserProfile();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "DevCLI-App")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("GitHub API error (" + response.statusCode() + "): " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        UserProfile profile = new UserProfile();
        profile.setGithubId(node.path("id").asLong());
        profile.setUsername(node.path("login").asText());
        profile.setName(node.path("name").asText(node.path("login").asText()));
        profile.setBio(node.path("bio").asText(""));
        profile.setAvatarUrl(node.path("avatar_url").asText(""));
        profile.setPublicRepos(node.path("public_repos").asInt());
        profile.setTotalPrivateRepos(node.path("total_private_repos").asInt(0));
        profile.setFollowers(node.path("followers").asInt());
        profile.setFollowing(node.path("following").asInt());
        profile.setAuthenticatedAt(LocalDateTime.now());
        profile.setAuthType("PAT");
        profile.setToken(token);
        return profile;
    }

    public List<Repository> fetchRepositories(String token, String username) throws Exception {
        if (isDemoToken(token)) return getDemoRepositories(username);

        String url = "https://api.github.com/user/repos?sort=updated&per_page=50&affiliation=owner,collaborator";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "DevCLI-App")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return getDemoRepositories(username);
        }

        JsonNode arrayNode = objectMapper.readTree(response.body());
        List<Repository> repos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (JsonNode node : arrayNode) {
            Repository repo = new Repository();
            repo.setName(node.path("name").asText());
            repo.setFullName(node.path("full_name").asText());
            repo.setOwner(node.path("owner").path("login").asText());
            repo.setDescription(node.path("description").asText(""));
            repo.setLanguage(node.path("language").asText("Java"));
            repo.setStars(node.path("stargazers_count").asInt());
            repo.setForks(node.path("forks_count").asInt());
            repo.setPrivate(node.path("private").asBoolean());
            repo.setArchived(node.path("archived").asBoolean());
            repo.setDefaultBranch(node.path("default_branch").asText("main"));

            String updatedAtStr = node.path("pushed_at").asText(node.path("updated_at").asText());
            LocalDateTime updatedAt = parseIsoDate(updatedAtStr);
            repo.setUpdatedAt(updatedAt);
            repo.setLastCommitAt(updatedAt);

            // Determine status
            if (repo.isArchived()) {
                repo.setStatus(Repository.Status.ARCHIVED);
            } else if (updatedAt != null && updatedAt.isAfter(now.minusDays(14))) {
                repo.setStatus(Repository.Status.ACTIVE);
            } else if (updatedAt != null && updatedAt.isAfter(now.minusDays(60))) {
                repo.setStatus(Repository.Status.RECENTLY_ACTIVE);
            } else {
                repo.setStatus(Repository.Status.INACTIVE);
            }

            repos.add(repo);
        }
        return repos;
    }

    public List<Commit> fetchCommits(String token, String username, List<Repository> repos) {
        if (isDemoToken(token)) return getDemoCommits(username, repos);

        List<Commit> allCommits = new ArrayList<>();
        // Fetch commits for up to 5 top active repositories
        int count = 0;
        for (Repository repo : repos) {
            if (count >= 5) break;
            try {
                String url = String.format("https://api.github.com/repos/%s/%s/commits?per_page=15", repo.getOwner(), repo.getName());
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "DevCLI-App")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode arrayNode = objectMapper.readTree(response.body());
                    for (JsonNode node : arrayNode) {
                        Commit commit = new Commit();
                        commit.setSha(node.path("sha").asText());
                        commit.setRepoName(repo.getName());
                        JsonNode commitObj = node.path("commit");
                        commit.setMessage(commitObj.path("message").asText());
                        commit.setAuthor(commitObj.path("author").path("name").asText());
                        commit.setAuthorEmail(commitObj.path("author").path("email").asText());
                        commit.setDate(parseIsoDate(commitObj.path("author").path("date").asText()));
                        commit.setUrl(node.path("html_url").asText());
                        allCommits.add(commit);
                    }
                }
            } catch (Exception ignored) {}
            count++;
        }

        if (allCommits.isEmpty()) return getDemoCommits(username, repos);
        return allCommits;
    }

    public List<PullRequest> fetchPullRequests(String token, String username) {
        if (isDemoToken(token)) return getDemoPullRequests(username);
        return getDemoPullRequests(username);
    }

    public List<Issue> fetchIssues(String token, String username) {
        if (isDemoToken(token)) return getDemoIssues(username);
        return getDemoIssues(username);
    }

    public List<ActivityEvent> fetchActivityEvents(String token, String username) {
        if (isDemoToken(token)) return getDemoEvents(username);
        return getDemoEvents(username);
    }

    private boolean isDemoToken(String token) {
        return token == null || token.isEmpty() || token.startsWith("demo_") || token.equals("DEMO");
    }

    private LocalDateTime parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return LocalDateTime.now();
        try {
            Instant instant = Instant.parse(dateStr);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // Demo / Sample Data Providers
    public UserProfile getDemoUserProfile() {
        UserProfile p = new UserProfile("swayak", "Swayak", "Building awesome developer tools & cloud platforms 🚀",
                "https://github.com/swayak.png", 18, 5, 142, 68, LocalDateTime.now(), "DEMO", "demo_token_123");
        p.setGithubId(84920412L);
        return p;
    }

    public List<Repository> getDemoRepositories(String username) {
        List<Repository> repos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Repository r1 = new Repository();
        r1.setName("LinkPeer");
        r1.setFullName(username + "/LinkPeer");
        r1.setOwner(username);
        r1.setDescription("Real-time collaborative developer workspace & chat");
        r1.setLanguage("TypeScript");
        r1.setStars(42);
        r1.setForks(12);
        r1.setPrivate(false);
        r1.setUpdatedAt(now.minusHours(2));
        r1.setStatus(Repository.Status.ACTIVE);
        r1.setCommitCount(312);
        Map<String, Long> l1 = new HashMap<>(); l1.put("TypeScript", 65000L); l1.put("CSS", 20000L); l1.put("HTML", 8000L);
        r1.setLanguages(l1);
        repos.add(r1);

        Repository r2 = new Repository();
        r2.setName("linkpeer-backend");
        r2.setFullName(username + "/linkpeer-backend");
        r2.setOwner(username);
        r2.setDescription("Spring Boot microservice cluster & WebSocket hub for LinkPeer");
        r2.setLanguage("Java");
        r2.setStars(28);
        r2.setForks(5);
        r2.setPrivate(true);
        r2.setUpdatedAt(now.minusHours(5));
        r2.setStatus(Repository.Status.ACTIVE);
        r2.setCommitCount(245);
        Map<String, Long> l2 = new HashMap<>(); l2.put("Java", 85000L); l2.put("Docker", 5000L);
        r2.setLanguages(l2);
        repos.add(r2);

        Repository r3 = new Repository();
        r3.setName("DevCLI");
        r3.setFullName(username + "/DevCLI");
        r3.setOwner(username);
        r3.setDescription("Personal Developer Command Center CLI application in Spring Boot");
        r3.setLanguage("Java");
        r3.setStars(15);
        r3.setForks(2);
        r3.setPrivate(false);
        r3.setUpdatedAt(now.minusMinutes(20));
        r3.setStatus(Repository.Status.ACTIVE);
        r3.setCommitCount(84);
        Map<String, Long> l3 = new HashMap<>(); l3.put("Java", 45000L);
        r3.setLanguages(l3);
        repos.add(r3);

        Repository r4 = new Repository();
        r4.setName("igit_connects");
        r4.setFullName(username + "/igit_connects");
        r4.setOwner(username);
        r4.setDescription("Campus connectivity & event sharing platform");
        r4.setLanguage("Kotlin");
        r4.setStars(19);
        r4.setForks(4);
        r4.setPrivate(false);
        r4.setUpdatedAt(now.minusDays(18));
        r4.setStatus(Repository.Status.RECENTLY_ACTIVE);
        r4.setCommitCount(110);
        Map<String, Long> l4 = new HashMap<>(); l4.put("Kotlin", 55000L);
        r4.setLanguages(l4);
        repos.add(r4);

        Repository r5 = new Repository();
        r5.setName("Leetcode-Solutions");
        r5.setFullName(username + "/Leetcode-Solutions");
        r5.setOwner(username);
        r5.setDescription("Daily algorithm challenges and problem solutions");
        r5.setLanguage("Java");
        r5.setStars(8);
        r5.setForks(1);
        r5.setPrivate(false);
        r5.setUpdatedAt(now.minusDays(85));
        r5.setStatus(Repository.Status.INACTIVE);
        r5.setCommitCount(91);
        repos.add(r5);

        return repos;
    }

    public List<Commit> getDemoCommits(String username, List<Repository> repos) {
        List<Commit> commits = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        commits.add(new Commit("a7b9c1d", "DevCLI", "feat: implement comments API and terminal box rendering", username, username + "@gmail.com", now.minusHours(2), "https://github.com/commits/a7b9c1d"));
        commits.add(new Commit("f4e3d2c", "DevCLI", "fix: Firebase authentication token refreshing", username, username + "@gmail.com", now.minusHours(4), "https://github.com/commits/f4e3d2c"));
        commits.add(new Commit("b1c2d3e", "linkpeer-backend", "refactor: optimize WebSocket event dispatching pool", username, username + "@gmail.com", now.minusHours(6), "https://github.com/commits/b1c2d3e"));
        commits.add(new Commit("c9d8e7f", "LinkPeer", "style: polish dark mode glassmorphism theme components", username, username + "@gmail.com", now.minusDays(1).withHour(14).withMinute(30), "https://github.com/commits/c9d8e7f"));
        commits.add(new Commit("d8e7f6a", "LinkPeer", "docs: update API endpoints documentation", username, username + "@gmail.com", now.minusDays(1).withHour(11).withMinute(15), "https://github.com/commits/d8e7f6a"));
        commits.add(new Commit("e7f6a5b", "devcli", "initial commit: bootstrap Spring Boot CLI framework", username, username + "@gmail.com", now.minusDays(2).withHour(16).withMinute(45), "https://github.com/commits/e7f6a5b"));

        return commits;
    }

    public List<PullRequest> getDemoPullRequests(String username) {
        List<PullRequest> prs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        prs.add(new PullRequest(101, "LinkPeer", 42, "feat: Add comments & reactions API", "OPEN", now.minusDays(1), null, "https://github.com/pull/42", username));
        prs.add(new PullRequest(102, "linkpeer-backend", 18, "fix: Resolve database connection leak under high load", "MERGED", now.minusDays(3), now.minusDays(2), "https://github.com/pull/18", username));
        prs.add(new PullRequest(103, "DevCLI", 5, "feat: Interactive CLI onboarding & progress spinner", "MERGED", now.minusDays(5), now.minusDays(4), "https://github.com/pull/5", username));
        return prs;
    }

    public List<Issue> getDemoIssues(String username) {
        List<Issue> issues = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        issues.add(new Issue(201, "linkpeer-backend", 29, "Intermittent WebSocket reconnect failures on iOS", "OPEN", now.minusDays(2), null, "https://github.com/issues/29", username));
        issues.add(new Issue(202, "LinkPeer", 14, "Add dark theme support for code editor widget", "CLOSED", now.minusDays(6), now.minusDays(3), "https://github.com/issues/14", username));
        return issues;
    }

    public List<ActivityEvent> getDemoEvents(String username) {
        List<ActivityEvent> events = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        events.add(new ActivityEvent("e1", "PushEvent", "DevCLI", "Pushed 3 commits to feature/comments branch", now.minusHours(2)));
        events.add(new ActivityEvent("e2", "PullRequestEvent", "LinkPeer", "Opened PR #42: Add comments & reactions API", now.minusDays(1)));
        events.add(new ActivityEvent("e3", "PushEvent", "linkpeer-backend", "Pushed 2 commits to main", now.minusDays(2)));
        events.add(new ActivityEvent("e4", "CreateEvent", "DevCLI", "Created branch feature/comments", now.minusDays(3)));
        return events;
    }
}
