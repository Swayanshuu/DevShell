package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import com.devcli.ui.ProgressRenderer;
import com.devcli.ui.TableRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Command(name = "stats", description = "View your Developer DNA report and interpreted stats", mixinStandardHelpOptions = true)
public class StatsCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final InsightEngine insightEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public StatsCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, InsightEngine insightEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.insightEngine = insightEngine;
        this.authService = authService;
        this.syncService = syncService;
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

        if (user == null) {
            System.out.println(AnsiStyle.brightRed("✗ No profile found. Run `devcli login` to authorize."));
            return;
        }

        BoxRenderer.printBanner("DEVELOPER DNA REPORT", "Analytical snapshot for @" + user.getUsername());

        // 1. Developer Profile Summary
        BoxRenderer.printSectionHeader("👤 DEVELOPER PROFILE");
        System.out.println("  Name       : " + AnsiStyle.boldWhite(user.getName()));
        System.out.println("  Handle     : " + AnsiStyle.cyan("@" + user.getUsername()));
        if (user.getGithubId() > 0) {
            System.out.println("  GitHub ID  : " + AnsiStyle.yellow("#" + user.getGithubId()));
        }
        System.out.println("  Bio        : " + AnsiStyle.dim(user.getBio()));
        System.out.println("  Network    : " + AnsiStyle.gray(user.getFollowers() + " followers • " + user.getFollowing() + " following"));
        System.out.println("  Repos      : " + AnsiStyle.green(repos.size() + " total accessible repos (" + user.getPublicRepos() + " public)"));

        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);
        List<String> langLines = new java.util.ArrayList<>();
        languages.forEach((lang, pct) -> {
            String progressBar = ProgressRenderer.buildProgressBar(pct, 20);
            langLines.add(String.format("  %s %s   %s", AnsiStyle.boldCyan(String.format("%-14s", lang)), progressBar, AnsiStyle.boldYellow(String.format("%3.0f%%", pct))));
        });
        if (langLines.isEmpty()) {
            langLines.add("  " + AnsiStyle.gray("No language stack data detected"));
        }
        BoxRenderer.renderBox("DEVELOPER STACK DISTRIBUTION", langLines, AnsiStyle.CYAN);
        System.out.println();

        // 3. Contribution Metrics
        int totalCommits = repos.stream().mapToInt(Repository::getCommitCount).sum();
        if (totalCommits == 0) totalCommits = commits.size();

        List<String> metricLines = new java.util.ArrayList<>();
        metricLines.add(String.format("  %s %s   %s", AnsiStyle.dim(String.format("%-20s", "Total Commits")), AnsiStyle.boldGreen(String.format("%-8s", String.valueOf(totalCommits))), AnsiStyle.gray("High commit volume")));
        metricLines.add(String.format("  %s %s   %s", AnsiStyle.dim(String.format("%-20s", "Active Projects")), AnsiStyle.boldCyan(String.format("%-8s", String.valueOf(repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE).count()))), AnsiStyle.gray("Multi-project focus")));
        metricLines.add(String.format("  %s %s   %s", AnsiStyle.dim(String.format("%-20s", "Pull Requests")), AnsiStyle.boldYellow(String.format("%-8s", String.valueOf(prs.size()))), AnsiStyle.gray("Active review flow")));
        metricLines.add(String.format("  %s %s   %s", AnsiStyle.dim(String.format("%-20s", "Commit Streak")), AnsiStyle.boldMagenta(String.format("%-8s", analysisEngine.calculateStreak(commits) + "d")), AnsiStyle.gray("Daily contribution habit")));

        BoxRenderer.renderBox("CONTRIBUTION METRICS", metricLines, AnsiStyle.GREEN);
        System.out.println();

        // 4. Developer Observations
        List<Insight> insights = insightEngine.generateInsights(repos, commits, prs, languages);
        List<String> obsLines = new java.util.ArrayList<>();
        for (Insight ins : insights) {
            obsLines.add("  " + AnsiStyle.boldMagenta(String.format("%-25s", ins.getTitle())) + AnsiStyle.cyan(ins.getMetric()));
            obsLines.add("    " + AnsiStyle.gray(ins.getDetail()));
        }
        if (obsLines.isEmpty()) {
            obsLines.add("  " + AnsiStyle.gray("Collecting data for observations"));
        }
        BoxRenderer.renderBox("DEVELOPER OBSERVATIONS", obsLines, AnsiStyle.YELLOW);
        System.out.println();
    }
}
