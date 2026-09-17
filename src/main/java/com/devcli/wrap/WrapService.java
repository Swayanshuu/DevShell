package com.devcli.wrap;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.model.UserProfile;
import com.devcli.service.AuthService;
import com.devcli.service.GitHubService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WrapService {

    private final GitHubService gitHubService;
    private final AuthService authService;

    public WrapService(
            GitHubService gitHubService,
            AuthService authService) {
        this.gitHubService = gitHubService;
        this.authService = authService;
    }

    public WrapData generateWrap(String period) throws Exception {
        return generateWrap(period, null);
    }

    public WrapData generateWrap(String period, WrapProgressListener listener) throws Exception {

        if (listener != null) listener.onProgress(10, "Fetching GitHub user profile...");

        UserProfile user = authService.getCurrentUser();

        if (user == null) {
            throw new IllegalStateException(
                    "You are not logged in. Run `devshell login` first.");
        }

        String token = user.getToken();
        String username = user.getUsername();

        if (listener != null) listener.onProgress(25, "Fetching repository index...");
        List<Repository> repositories = gitHubService.fetchRepositories(token, username);

        if (listener != null) listener.onProgress(40, "Fetching pull requests & activity...");
        List<PullRequest> allPullRequests = gitHubService.fetchPullRequests(
                token,
                username);

        // Determine WRAP period
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (period.toLowerCase()) {
            case "week" -> start = now.minusWeeks(1);
            case "month" -> start = now.minusMonths(1);
            case "year" -> start = now.minusYears(1);
            default -> throw new IllegalArgumentException(
                    "Invalid WRAP period: " + period);
        }

        if (listener != null) listener.onProgress(50, "Analyzing commits & diff stats for " + period.toUpperCase() + "...");
        List<Commit> commits = gitHubService.fetchCommitsForPeriod(
                token,
                username,
                repositories,
                start,
                now,
                (currentRepo, totalRepos, repoName) -> {
                    if (listener != null) {
                        int pct = 50 + (int) (((double) currentRepo / Math.max(1, totalRepos)) * 35);
                        listener.onProgress(pct, "Analyzing commits: " + repoName + " (" + currentRepo + "/" + totalRepos + ")...");
                    }
                });

        if (listener != null) listener.onProgress(88, "Calculating coding hours & streak analytics...");

        // Filter pull requests in period
        List<PullRequest> pullRequests = allPullRequests.stream()
                .filter(pr -> pr.getCreatedAt() != null &&
                        !pr.getCreatedAt().isBefore(start) &&
                        !pr.getCreatedAt().isAfter(now))
                .toList();

        int additions = commits.stream()
                .mapToInt(Commit::getAdditions)
                .sum();

        int deletions = commits.stream()
                .mapToInt(Commit::getDeletions)
                .sum();

        int linesChanged = additions + deletions;
        double codingHours = calculateCodingHours(commits);

        // Active days calculation
        Set<LocalDate> activeDates = new HashSet<>();
        for (Commit c : commits) {
            if (c.getDate() != null) {
                activeDates.add(c.getDate().toLocalDate());
            }
        }
        for (PullRequest pr : pullRequests) {
            if (pr.getCreatedAt() != null) {
                activeDates.add(pr.getCreatedAt().toLocalDate());
            }
        }
        int activeDays = activeDates.size();
        int longestStreak = calculateLongestStreak(activeDates);

        // Projects (Repositories with user activity in period)
        Set<String> activeProjects = new HashSet<>();
        for (Commit c : commits) {
            if (c.getRepoName() != null && !c.getRepoName().isBlank()) {
                activeProjects.add(c.getRepoName());
            }
        }
        for (PullRequest pr : pullRequests) {
            if (pr.getRepoName() != null && !pr.getRepoName().isBlank()) {
                activeProjects.add(pr.getRepoName());
            }
        }
        int projects = activeProjects.size();

        String topLanguage = findTopLanguages(commits, repositories);
        String topRepository = findTopRepositories(commits, pullRequests, repositories);

        String displayName = (user.getName() != null && !user.getName().isBlank())
                ? user.getName()
                : username;
        String userHandle = username.startsWith("@") ? username : "@" + username;
        String avatarUrl = user.getAvatarUrl();
        if ((avatarUrl == null || avatarUrl.isBlank()) && username != null && !username.isBlank()) {
            String cleanUser = username.startsWith("@") ? username.substring(1) : username;
            avatarUrl = "https://github.com/" + cleanUser + ".png";
        }

        return new WrapData(
                period.toUpperCase(),
                commits.size(),
                codingHours,
                additions,
                deletions,
                linesChanged,
                activeDays,
                longestStreak,
                projects,
                topLanguage,
                topRepository,
                userHandle,
                displayName,
                avatarUrl);
    }

    public double calculateCodingHours(List<Commit> commits) {
        if (commits == null || commits.isEmpty()) {
            return 0.0;
        }

        List<LocalDateTime> timestamps = commits.stream()
                .map(Commit::getDate)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (timestamps.isEmpty()) {
            return 0.0;
        }

        double totalMinutes = 0;
        LocalDateTime sessionStart = timestamps.get(0);
        LocalDateTime lastCommit = sessionStart;

        for (int i = 1; i < timestamps.size(); i++) {
            LocalDateTime current = timestamps.get(i);
            long minutesBetween = java.time.Duration.between(lastCommit, current).toMinutes();

            if (minutesBetween <= 120) {
                lastCommit = current;
            } else {
                long sessionMinutes = java.time.Duration.between(sessionStart, lastCommit).toMinutes() + 30;
                totalMinutes += sessionMinutes;
                sessionStart = current;
                lastCommit = current;
            }
        }

        long finalSessionMinutes = java.time.Duration.between(sessionStart, lastCommit).toMinutes() + 30;
        totalMinutes += finalSessionMinutes;

        double hours = totalMinutes / 60.0;
        return Math.round(hours * 10.0) / 10.0;
    }

    public int calculateLongestStreak(Set<LocalDate> activeDates) {
        if (activeDates == null || activeDates.isEmpty()) return 0;
        List<LocalDate> sorted = new ArrayList<>(activeDates);
        Collections.sort(sorted);
        int maxStreak = 1;
        int currentStreak = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
            } else {
                currentStreak = 1;
            }
        }
        return maxStreak;
    }

    public String findTopLanguages(List<Commit> commits, List<Repository> repositories) {
        Map<String, String> repoLanguageMap = new HashMap<>();
        if (repositories != null) {
            for (Repository repo : repositories) {
                if (repo.getName() != null && repo.getLanguage() != null && !repo.getLanguage().isBlank()) {
                    repoLanguageMap.put(repo.getName().toLowerCase(), repo.getLanguage());
                }
            }
        }

        Map<String, Long> langCommitCounts = new LinkedHashMap<>();
        if (commits != null && !commits.isEmpty()) {
            for (Commit commit : commits) {
                if (commit.getRepoName() != null) {
                    String lang = repoLanguageMap.get(commit.getRepoName().toLowerCase());
                    if (lang != null && !lang.isBlank() && !"Unknown".equalsIgnoreCase(lang)) {
                        langCommitCounts.put(lang, langCommitCounts.getOrDefault(lang, 0L) + 1);
                    }
                }
            }
        }

        if (langCommitCounts.isEmpty() && repositories != null) {
            for (Repository repo : repositories) {
                String lang = repo.getLanguage();
                if (lang != null && !lang.isBlank() && !"Unknown".equalsIgnoreCase(lang)) {
                    langCommitCounts.put(lang, langCommitCounts.getOrDefault(lang, 0L) + 1);
                }
            }
        }

        List<String> top3 = langCommitCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        if (top3.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", top3);
    }

    public String findTopLanguage(List<Commit> commits, List<Repository> repositories) {
        String langs = findTopLanguages(commits, repositories);
        return langs.contains(",") ? langs.split(",")[0].trim() : langs;
    }

    public String findTopRepositories(List<Commit> commits, List<PullRequest> pullRequests, List<Repository> repositories) {
        Set<String> privateRepoNames = new HashSet<>();
        if (repositories != null) {
            for (Repository repo : repositories) {
                if (repo.isPrivate() && repo.getName() != null) {
                    privateRepoNames.add(repo.getName().toLowerCase());
                }
            }
        }

        Map<String, Long> activityCounts = new LinkedHashMap<>();
        if (commits != null) {
            for (Commit c : commits) {
                if (c.getRepoName() != null && !c.getRepoName().isBlank()) {
                    String name = c.getRepoName();
                    if (!privateRepoNames.contains(name.toLowerCase())) {
                        activityCounts.put(name, activityCounts.getOrDefault(name, 0L) + 1);
                    }
                }
            }
        }
        if (pullRequests != null) {
            for (PullRequest pr : pullRequests) {
                if (pr.getRepoName() != null && !pr.getRepoName().isBlank()) {
                    String name = pr.getRepoName();
                    if (!privateRepoNames.contains(name.toLowerCase())) {
                        activityCounts.put(name, activityCounts.getOrDefault(name, 0L) + 1);
                    }
                }
            }
        }
        if (activityCounts.isEmpty() && repositories != null) {
            for (Repository repo : repositories) {
                if (!repo.isPrivate() && repo.getName() != null && !repo.getName().isBlank()) {
                    activityCounts.put(repo.getName(), 1L);
                }
            }
        }

        List<String> top3 = activityCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        if (top3.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", top3);
    }

    public String findTopRepository(List<Commit> commits, List<PullRequest> pullRequests, List<Repository> repositories) {
        String repos = findTopRepositories(commits, pullRequests, repositories);
        return repos.contains(",") ? repos.split(",")[0].trim() : repos;
    }
}