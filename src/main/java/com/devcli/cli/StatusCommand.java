package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Command(name = "status", description = "Show your Personal Developer Command Center snapshot", mixinStandardHelpOptions = true)
public class StatusCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final SyncService syncService;
    private final AuthService authService;

    @Autowired
    public StatusCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, InsightEngine insightEngine,
            JournalService journalService, SyncService syncService, AuthService authService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.syncService = syncService;
        this.authService = authService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        UserProfile user = storageService.getUserProfile();
        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);

        BoxRenderer.printAsciiBanner();

        // 1. TODAY SUMMARY
        List<Commit> todayCommits = getTodayCommits(commits);
        int additions = analysisEngine.getTodayAdditions(commits);
        int deletions = analysisEngine.getTodayDeletions(commits);
        long activeReposCount = repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE || r.getStatus() == Repository.Status.RECENTLY_ACTIVE).count();
        long todayPrsCount = prs != null ? prs.stream().filter(p -> p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().equals(LocalDate.now())).count() : 0;
        int todayReviews = (int) (todayPrsCount > 0 ? todayPrsCount : Math.min(todayCommits.size(), 1));

        List<String> todayLines = new ArrayList<>();
        todayLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Commits")), AnsiStyle.boldWhite(String.valueOf(todayCommits.size()))));
        todayLines.add(String.format("  %s %s / %s", AnsiStyle.dim(String.format("%-22s", "Lines changed")), AnsiStyle.boldGreen("+" + additions), AnsiStyle.boldRed("-" + deletions)));
        todayLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Active Repositories")), AnsiStyle.boldCyan(String.valueOf(activeReposCount))));
        todayLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Pull Requests")), AnsiStyle.boldYellow(String.valueOf(todayPrsCount))));
        todayLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Code Reviews")), AnsiStyle.boldMagenta(String.valueOf(todayReviews))));

        BoxRenderer.renderBox("TODAY'S ACTIVITY", todayLines, AnsiStyle.CYAN);
        System.out.println();

        // 2. STREAK & VELOCITY
        int currentStreak = analysisEngine.calculateStreak(commits);
        int longestStreak = analysisEngine.calculateLongestStreak(commits);
        if (longestStreak < currentStreak) longestStreak = currentStreak;

        List<String> streakLines = new ArrayList<>();
        int gaugeFill = Math.min(18, (int) Math.round((currentStreak / Math.max(1.0, (double) longestStreak)) * 18));
        String streakBar = "█".repeat(gaugeFill) + "░".repeat(Math.max(0, 18 - gaugeFill));

        streakLines.add(String.format("  %s %-7s %s", AnsiStyle.dim(String.format("%-22s", "Current Streak")), AnsiStyle.boldYellow(currentStreak + " days"), AnsiStyle.boldCyan("[" + streakBar + "]")));
        streakLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Longest Streak")), AnsiStyle.boldGreen(longestStreak + " days")));

        BoxRenderer.renderBox("STREAK & VELOCITY", streakLines, AnsiStyle.YELLOW);
        System.out.println();

        // 3. TOP STACK
        List<String> stackLines = new ArrayList<>();
        if (!languages.isEmpty()) {
            int count = 0;
            for (Map.Entry<String, Double> entry : languages.entrySet()) {
                if (count >= 3) break;
                double pct = entry.getValue();
                int barLen = Math.min(18, (int) Math.round((pct / 100.0) * 18));
                String bar = "█".repeat(barLen) + "░".repeat(Math.max(0, 18 - barLen));
                stackLines.add(String.format("  %s %s   %s", AnsiStyle.boldWhite(String.format("%-14s", entry.getKey())), AnsiStyle.dim(String.format("%3.0f%%", pct)), AnsiStyle.cyan("[" + bar + "]")));
                count++;
            }
        } else {
            stackLines.add("  " + AnsiStyle.gray("No stack data collected yet"));
        }

        BoxRenderer.renderBox("TOP STACK DISTRIBUTION", stackLines, AnsiStyle.GREEN);
        System.out.println();

        // 4. RECENT PROJECTS
        List<String> projectLines = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        List<Repository> recentRepos = repos.stream()
                .filter(r -> r.getName() != null && seenNames.add(r.getName().toLowerCase()))
                .filter(r -> r.getLastCommitAt() != null || r.getUpdatedAt() != null)
                .sorted((r1, r2) -> {
                    LocalDateTime t1 = r1.getLastCommitAt() != null ? r1.getLastCommitAt() : r1.getUpdatedAt();
                    LocalDateTime t2 = r2.getLastCommitAt() != null ? r2.getLastCommitAt() : r2.getUpdatedAt();
                    return t2.compareTo(t1);
                })
                .limit(3)
                .collect(Collectors.toList());

        if (!recentRepos.isEmpty()) {
            for (Repository repo : recentRepos) {
                LocalDateTime t = repo.getLastCommitAt() != null ? repo.getLastCommitAt() : repo.getUpdatedAt();
                String relTime = analysisEngine.getRelativeTime(t);
                String langStr = repo.getLanguage() != null ? repo.getLanguage() : "General";
                projectLines.add(String.format("  %s %s %s", AnsiStyle.boldCyan(String.format("%-18s", repo.getName())), AnsiStyle.dim(String.format("%-10s", relTime)), AnsiStyle.gray("[" + langStr + "]")));
            }
        } else {
            projectLines.add("  " + AnsiStyle.gray("No recent projects detected"));
        }

        BoxRenderer.renderBox("RECENT PROJECTS", projectLines, AnsiStyle.MAGENTA);
        System.out.println();
    }

    private List<Commit> getTodayCommits(List<Commit> commits) {
        LocalDate today = LocalDate.now();
        List<Commit> list = new ArrayList<>();
        if (commits != null) {
            for (Commit c : commits) {
                if (c.getDate() != null && c.getDate().toLocalDate().equals(today)) {
                    list.add(c);
                }
            }
        }
        return list;
    }
}
