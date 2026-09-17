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
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Authentication required. Run devshell login to authenticate.");
        }

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
        if (token == null || token.trim().isEmpty())
            return new ArrayList<>();

        String url = "https://api.github.com/user/repos?sort=updated&per_page=100&affiliation=owner,collaborator,organization_member";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "DevCLI-App")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return new ArrayList<>();
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
            repo.setLanguage(node.path("language").asText(null));
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
        if (isDemoToken(token))
            return new ArrayList<>();

        List<Commit> allCommits = new ArrayList<>();
        // Fetch commits for up to 5 top active repositories
        int count = 0;
        for (Repository repo : repos) {
            if (count >= 5)
                break;
            try {
                String url = String.format("https://api.github.com/repos/%s/%s/commits?per_page=15", repo.getOwner(),
                        repo.getName());
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
            } catch (Exception ignored) {
            }
            count++;
        }

        return allCommits;
    }

    @FunctionalInterface
    public interface RepoScanCallback {
        void onRepoScan(int current, int total, String repoName);
    }

    public List<Commit> fetchCommitsForPeriod(
            String token,
            String username,
            List<Repository> repos,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return fetchCommitsForPeriod(token, username, repos, start, end, null);
    }

    public List<Commit> fetchCommitsForPeriod(
            String token,
            String username,
            List<Repository> repos,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end,
            RepoScanCallback callback) {

        if (isDemoToken(token) || repos == null || repos.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter repos to only those active/updated around or after 'start'
        List<Repository> targetRepos = repos.stream()
                .filter(r -> r.getUpdatedAt() == null || !r.getUpdatedAt().isBefore(start.minusDays(1)))
                .toList();

        if (targetRepos.isEmpty()) {
            // Fallback to top 10 repos if filtering yielded none
            targetRepos = repos.stream().limit(10).toList();
        }

        List<Commit> allCommits = java.util.Collections.synchronizedList(new ArrayList<>());

        String since = java.time.format.DateTimeFormatter.ISO_INSTANT
                .format(start.atZone(java.time.ZoneId.systemDefault()).toInstant());

        String until = java.time.format.DateTimeFormatter.ISO_INSTANT
                .format(end.atZone(java.time.ZoneId.systemDefault()).toInstant());

        int totalRepos = targetRepos.size();
        java.util.concurrent.atomic.AtomicInteger completedCounter = new java.util.concurrent.atomic.AtomicInteger(0);

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(
                Math.min(8, Math.max(1, totalRepos)));

        for (Repository repo : targetRepos) {
            executor.submit(() -> {
                try {
                    int page = 1;
                    int maxPages = 3;

                    while (page <= maxPages) {
                        String url = String.format(
                                "https://api.github.com/repos/%s/%s/commits" +
                                        "?author=%s&since=%s&until=%s&per_page=100&page=%d",
                                repo.getOwner(),
                                repo.getName(),
                                username,
                                java.net.URLEncoder.encode(since, java.nio.charset.StandardCharsets.UTF_8),
                                java.net.URLEncoder.encode(until, java.nio.charset.StandardCharsets.UTF_8),
                                page);

                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Authorization", "Bearer " + token)
                                .header("Accept", "application/vnd.github.v3+json")
                                .header("User-Agent", "DevCLI-App")
                                .GET()
                                .build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() != 200) {
                            break;
                        }

                        JsonNode arrayNode = objectMapper.readTree(response.body());
                        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
                            break;
                        }

                        List<Commit> repoCommits = new ArrayList<>();
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

                            repoCommits.add(commit);
                        }

                        // Fetch exact real contributor line stats from GitHub API
                        int repoAdditions = 0;
                        int repoDeletions = 0;
                        try {
                            String statsUrl = String.format("https://api.github.com/repos/%s/%s/stats/contributors", repo.getOwner(), repo.getName());
                            HttpRequest statsReq = HttpRequest.newBuilder()
                                    .uri(URI.create(statsUrl))
                                    .header("Authorization", "Bearer " + token)
                                    .header("Accept", "application/vnd.github.v3+json")
                                    .header("User-Agent", "DevCLI-App")
                                    .GET()
                                    .build();

                            for (int attempt = 0; attempt < 2; attempt++) {
                                HttpResponse<String> statsResp = httpClient.send(statsReq, HttpResponse.BodyHandlers.ofString());
                                if (statsResp.statusCode() == 200) {
                                    JsonNode rootNode = objectMapper.readTree(statsResp.body());
                                    if (rootNode.isArray()) {
                                        long startEpoch = start.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
                                        long endEpoch = end.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
                                        for (JsonNode contrib : rootNode) {
                                            String login = contrib.path("author").path("login").asText("");
                                            if (login.equalsIgnoreCase(username) || username.toLowerCase().contains(login.toLowerCase())) {
                                                JsonNode weeks = contrib.path("weeks");
                                                if (weeks.isArray()) {
                                                    for (JsonNode w : weeks) {
                                                        long weekStart = w.path("w").asLong(0);
                                                        if (weekStart >= startEpoch - (86400 * 7) && weekStart <= endEpoch) {
                                                            repoAdditions += w.path("a").asInt(0);
                                                            repoDeletions += w.path("d").asInt(0);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                } else if (statsResp.statusCode() == 202) {
                                    Thread.sleep(300);
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}

                        int commitsCount = Math.max(1, repoCommits.size());
                        int perCommitAdd = repoAdditions / commitsCount;
                        int perCommitDel = repoDeletions / commitsCount;
                        for (Commit c : repoCommits) {
                            c.setAdditions(perCommitAdd);
                            c.setDeletions(perCommitDel);
                        }

                        allCommits.addAll(repoCommits);

                        if (arrayNode.size() < 100) {
                            break;
                        }
                        page++;
                    }
                } catch (Exception ignored) {
                } finally {
                    int done = completedCounter.incrementAndGet();
                    if (callback != null) {
                        callback.onRepoScan(done, totalRepos, repo.getName());
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(15, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new ArrayList<>(allCommits);
    }

    public List<PullRequest> fetchPullRequests(String token, String username) {
        if (isDemoToken(token) || username == null || username.isEmpty())
            return new ArrayList<>();

        List<PullRequest> prs = new ArrayList<>();
        try {
            String url = String.format(
                    "https://api.github.com/search/issues?q=author:%s+type:pr&sort=updated&per_page=30", username);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "DevCLI-App")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode items = rootNode.path("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        PullRequest pr = new PullRequest();
                        pr.setId(item.path("id").asInt());
                        pr.setNumber(item.path("number").asInt());
                        pr.setTitle(item.path("title").asText());
                        pr.setAuthor(username);

                        String repoUrl = item.path("repository_url").asText();
                        String repoName = repoUrl.contains("/") ? repoUrl.substring(repoUrl.lastIndexOf('/') + 1)
                                : "repo";
                        pr.setRepoName(repoName);

                        String state = item.path("state").asText("OPEN").toUpperCase();
                        if ("CLOSED".equals(state) && item.has("pull_request")
                                && !item.path("pull_request").path("merged_at").isNull()) {
                            state = "MERGED";
                        }
                        pr.setState(state);

                        pr.setCreatedAt(parseIsoDate(item.path("created_at").asText()));
                        pr.setMergedAt(parseIsoDate(item.path("closed_at").asText()));
                        pr.setUrl(item.path("html_url").asText());
                        prs.add(pr);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return prs;
    }

    public List<Issue> fetchIssues(String token, String username) {
        if (isDemoToken(token) || username == null || username.isEmpty())
            return new ArrayList<>();

        List<Issue> issues = new ArrayList<>();
        try {
            String url = String.format(
                    "https://api.github.com/search/issues?q=author:%s+type:issue&sort=updated&per_page=30", username);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "DevCLI-App")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode items = rootNode.path("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        Issue issue = new Issue();
                        issue.setId(item.path("id").asInt());
                        issue.setNumber(item.path("number").asInt());
                        issue.setTitle(item.path("title").asText());
                        issue.setAuthor(username);

                        String repoUrl = item.path("repository_url").asText();
                        String repoName = repoUrl.contains("/") ? repoUrl.substring(repoUrl.lastIndexOf('/') + 1)
                                : "repo";
                        issue.setRepoName(repoName);

                        issue.setState(item.path("state").asText("OPEN").toUpperCase());
                        issue.setCreatedAt(parseIsoDate(item.path("created_at").asText()));
                        issue.setClosedAt(parseIsoDate(item.path("closed_at").asText()));
                        issue.setUrl(item.path("html_url").asText());
                        issues.add(issue);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return issues;
    }

    public List<ActivityEvent> fetchActivityEvents(String token, String username) {
        if (isDemoToken(token) || username == null || username.isEmpty())
            return new ArrayList<>();

        List<ActivityEvent> events = new ArrayList<>();
        try {
            String url = String.format("https://api.github.com/users/%s/events?per_page=30", username);
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
                if (arrayNode != null && arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        String id = node.path("id").asText();
                        String type = node.path("type").asText();
                        String repoName = node.path("repo").path("name").asText();
                        if (repoName.contains("/")) {
                            repoName = repoName.substring(repoName.indexOf('/') + 1);
                        }
                        LocalDateTime createdAt = parseIsoDate(node.path("created_at").asText());
                        JsonNode payload = node.path("payload");

                        String detail = type;
                        if ("PushEvent".equalsIgnoreCase(type)) {
                            int count = payload.path("commits").size();
                            String branch = payload.path("ref").asText("").replace("refs/heads/", "");
                            detail = "Pushed " + count + " commit" + (count != 1 ? "s" : "")
                                    + (branch.isEmpty() ? "" : " to " + branch);
                        } else if ("CreateEvent".equalsIgnoreCase(type)) {
                            String refType = payload.path("ref_type").asText("item");
                            String ref = payload.path("ref").asText("");
                            detail = "Created " + refType + (ref.isEmpty() ? "" : " " + ref);
                        } else if ("PullRequestEvent".equalsIgnoreCase(type)) {
                            String action = payload.path("action").asText("updated");
                            int num = payload.path("number").asInt();
                            detail = action.substring(0, 1).toUpperCase() + action.substring(1) + " PR #" + num;
                        } else if ("IssuesEvent".equalsIgnoreCase(type)) {
                            String action = payload.path("action").asText("updated");
                            int num = payload.path("issue").path("number").asInt();
                            detail = action.substring(0, 1).toUpperCase() + action.substring(1) + " issue #" + num;
                        } else if ("WatchEvent".equalsIgnoreCase(type)) {
                            detail = "Starred repository";
                        }

                        events.add(new ActivityEvent(id, type, repoName, detail, createdAt));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return events;
    }

    private boolean isDemoToken(String token) {
        return token == null || token.isEmpty() || token.startsWith("demo_") || token.equals("DEMO");
    }

    private LocalDateTime parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return LocalDateTime.now();
        try {
            Instant instant = Instant.parse(dateStr);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
