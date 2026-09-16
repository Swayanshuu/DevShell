package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Command(name = "trends", description = "Track historical trends over 30d, 90d, or 365d windows", mixinStandardHelpOptions = true)
public class TrendsCommand implements Runnable {

    @Option(names = {"--30d"}, description = "Analyze trends over the last 30 days")
    private boolean days30;

    @Option(names = {"--90d"}, description = "Analyze trends over the last 90 days")
    private boolean days90;

    @Option(names = {"--365d"}, description = "Analyze trends over the last 365 days")
    private boolean days365;

    private final LocalStorageService storageService;
    private final TrendEngine trendEngine;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public TrendsCommand(LocalStorageService storageService, TrendEngine trendEngine, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.trendEngine = trendEngine;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        int days = 30;
        if (days90) days = 90;
        else if (days365) days = 365;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();

        TrendEngine.TrendMetrics trend = trendEngine.calculateTrend(days, commits, prs, repos, analysisEngine);

        System.out.println();
        
        List<String> activityLines = new ArrayList<>();
        activityLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Commits")), AnsiStyle.boldWhite(String.valueOf(trend.getCurrentCommits()))));
        activityLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Repositories")), AnsiStyle.boldCyan(String.valueOf(trend.getActiveRepos()))));
        activityLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "PRs")), AnsiStyle.boldYellow(String.valueOf(trend.getCurrentPrs()))));
        activityLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Reviews")), AnsiStyle.boldMagenta(String.valueOf(Math.max(0, trend.getCurrentPrs())))));
        
        BoxRenderer.renderBox("LAST " + days + " DAYS ACTIVITY", activityLines, AnsiStyle.CYAN);
        System.out.println();

        List<String> stackLines = new ArrayList<>();
        int langCount = 0;
        for (Map.Entry<String, Double> entry : trend.getTopLanguages().entrySet()) {
            if (langCount >= 3) break;
            double pct = entry.getValue();
            int barLen = Math.min(15, (int) Math.round((pct / 100.0) * 15));
            String bar = "█".repeat(barLen) + "░".repeat(Math.max(0, 15 - barLen));
            stackLines.add(String.format("  %s %s   %s",
                    AnsiStyle.boldWhite(String.format("%-14s", entry.getKey())),
                    AnsiStyle.cyan("[" + bar + "]"),
                    AnsiStyle.boldYellow(String.format("%3.0f%%", pct))));
            langCount++;
        }
        if (stackLines.isEmpty()) {
            stackLines.add("  " + AnsiStyle.gray("No stack data collected"));
        }
        
        BoxRenderer.renderBox("TOP STACK DISTRIBUTION", stackLines, AnsiStyle.GREEN);
        System.out.println();

        List<String> compareLines = new ArrayList<>();
        String commitDeltaStr = trend.getCommitChangePct() >= 0 ? "+" + String.format("%.0f%%", trend.getCommitChangePct()) : String.format("%.0f%%", trend.getCommitChangePct());
        String prDeltaStr = trend.getPrChangePct() >= 0 ? "+" + String.format("%.0f%%", trend.getPrChangePct()) : String.format("%.0f%%", trend.getPrChangePct());

        compareLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Commits")), trend.getCommitChangePct() >= 0 ? AnsiStyle.boldGreen(commitDeltaStr) : AnsiStyle.boldRed(commitDeltaStr)));
        compareLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "PRs")), trend.getPrChangePct() >= 0 ? AnsiStyle.boldGreen(prDeltaStr) : AnsiStyle.boldRed(prDeltaStr)));

        BoxRenderer.renderBox("COMPARISON TO PREVIOUS PERIOD", compareLines, AnsiStyle.YELLOW);
        System.out.println();
    }
}
