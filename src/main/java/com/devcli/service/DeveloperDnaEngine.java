package com.devcli.service;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeveloperDnaEngine {

    public static class DnaProfile {
        private final Map<String, Double> categoryBreakdown;
        private final String primaryDomain;
        private final String peakProductivityWindow;
        private final int longestStreakDays;
        private final long totalPublicRepos;
        private final long totalPrs;

        public DnaProfile(Map<String, Double> categoryBreakdown, String primaryDomain, String peakProductivityWindow, int longestStreakDays, long totalPublicRepos, long totalPrs) {
            this.categoryBreakdown = categoryBreakdown;
            this.primaryDomain = primaryDomain;
            this.peakProductivityWindow = peakProductivityWindow;
            this.longestStreakDays = longestStreakDays;
            this.totalPublicRepos = totalPublicRepos;
            this.totalPrs = totalPrs;
        }

        public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
        public String getPrimaryDomain() { return primaryDomain; }
        public String getPeakProductivityWindow() { return peakProductivityWindow; }
        public int getLongestStreakDays() { return longestStreakDays; }
        public long getTotalPublicRepos() { return totalPublicRepos; }
        public long getTotalPrs() { return totalPrs; }
    }

    public DnaProfile analyzeDna(List<Repository> repos, List<Commit> commits, List<PullRequest> prs, AnalysisEngine analysisEngine) {
        Map<String, Double> categories = new LinkedHashMap<>();
        double backendWeight = 0, mobileWeight = 0, databaseWeight = 0, frontendWeight = 0, otherWeight = 0;

        if (repos != null) {
            for (Repository r : repos) {
                String lang = r.getLanguage().toLowerCase();
                String name = r.getName().toLowerCase();
                double weight = r.getCommitCount() > 0 ? r.getCommitCount() : 10;

                if (lang.contains("java") || lang.contains("python") || lang.contains("go") || lang.contains("rust") || lang.contains("c#") || name.contains("api") || name.contains("server") || name.contains("backend")) {
                    backendWeight += weight;
                } else if (lang.contains("dart") || lang.contains("kotlin") || lang.contains("swift") || name.contains("flutter") || name.contains("android") || name.contains("ios") || name.contains("mobile")) {
                    mobileWeight += weight;
                } else if (lang.contains("sql") || name.contains("db") || name.contains("database") || name.contains("postgres")) {
                    databaseWeight += weight;
                } else if (lang.contains("typescript") || lang.contains("javascript") || lang.contains("html") || lang.contains("css") || name.contains("web") || name.contains("ui") || name.contains("frontend")) {
                    frontendWeight += weight;
                } else {
                    otherWeight += weight;
                }
            }
        }

        double total = backendWeight + mobileWeight + databaseWeight + frontendWeight + otherWeight;
        if (total == 0) total = 1.0;

        categories.put("Backend", Math.round((backendWeight / total) * 100.0 * 10.0) / 10.0);
        categories.put("Mobile", Math.round((mobileWeight / total) * 100.0 * 10.0) / 10.0);
        categories.put("Database", Math.round((databaseWeight / total) * 100.0 * 10.0) / 10.0);
        categories.put("Frontend", Math.round((frontendWeight / total) * 100.0 * 10.0) / 10.0);
        categories.put("Other", Math.round((otherWeight / total) * 100.0 * 10.0) / 10.0);

        String primaryDomain = categories.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Generalist");

        // Peak productivity time window calculation
        int morningCount = 0, afternoonCount = 0, eveningCount = 0, nightCount = 0;
        if (commits != null) {
            for (Commit c : commits) {
                if (c.getDate() != null) {
                    int hour = c.getDate().getHour();
                    if (hour >= 6 && hour < 12) morningCount++;
                    else if (hour >= 12 && hour < 17) afternoonCount++;
                    else if (hour >= 17 && hour < 22) eveningCount++;
                    else nightCount++;
                }
            }
        }

        String peakWindow = "8 PM - 12 AM";
        int maxCommits = nightCount;
        if (morningCount > maxCommits) { peakWindow = "6 AM - 12 PM"; maxCommits = morningCount; }
        if (afternoonCount > maxCommits) { peakWindow = "12 PM - 5 PM"; maxCommits = afternoonCount; }
        if (eveningCount > maxCommits) { peakWindow = "5 PM - 8 PM"; maxCommits = eveningCount; }

        int longestStreak = analysisEngine.calculateLongestStreak(commits);
        long publicRepos = repos != null ? repos.stream().filter(r -> !r.isPrivate()).count() : 0;
        long totalPrsCount = prs != null ? prs.size() : 0;

        return new DnaProfile(categories, primaryDomain, peakWindow, longestStreak, publicRepos, totalPrsCount);
    }
}
