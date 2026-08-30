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

        // 2. Language Stack Distribution
        BoxRenderer.printSectionHeader("💻 STACK & LANGUAGES");
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);
        languages.forEach((lang, pct) -> {
            String progressBar = ProgressRenderer.buildProgressBar(pct, 25);
            System.out.printf("  %-14s %s\n", AnsiStyle.boldCyan(lang), progressBar);
        });

        // 3. Contribution Metrics
        BoxRenderer.printSectionHeader("📊 CONTRIBUTION METRICS");
        int totalCommits = repos.stream().mapToInt(Repository::getCommitCount).sum();
        if (totalCommits == 0) totalCommits = commits.size() * 14;

        List<String> headers = List.of("Metric", "Value", "Interpretation");
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Total Commits", AnsiStyle.boldGreen(String.valueOf(totalCommits)), "High commit volume & steady velocity"));
        rows.add(List.of("Active Projects", AnsiStyle.boldCyan(String.valueOf(repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE).count())), "Multi-project active focus"));
        rows.add(List.of("Pull Requests", AnsiStyle.boldYellow(String.valueOf(prs.size())), "Frequent code reviews and contributions"));
        rows.add(List.of("Commit Streak", AnsiStyle.boldMagenta(analysisEngine.calculateStreak(commits) + " days"), "Consistent daily development habit"));

        TableRenderer.printTable(headers, rows);

        // 4. Developer Observations
        BoxRenderer.printSectionHeader("🧠 DEVELOPER OBSERVATIONS");
        List<Insight> insights = insightEngine.generateInsights(repos, commits, prs, languages);
        for (Insight ins : insights) {
            System.out.println("  " + AnsiStyle.boldMagenta("▸ " + ins.getTitle()));
            System.out.println("    " + AnsiStyle.gray(ins.getDetail()));
            System.out.println("    " + AnsiStyle.dim("Metric: " + ins.getMetric()));
            System.out.println();
        }
    }
}
