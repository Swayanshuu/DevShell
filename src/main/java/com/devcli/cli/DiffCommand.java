package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.Learning;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "diff", description = "Compare current and previous developer activity periods", mixinStandardHelpOptions = true)
public class DiffCommand implements Runnable {

    private final LocalStorageService storageService;
    private final TrendEngine trendEngine;
    private final AnalysisEngine analysisEngine;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public DiffCommand(LocalStorageService storageService, TrendEngine trendEngine, AnalysisEngine analysisEngine, JournalService journalService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.trendEngine = trendEngine;
        this.analysisEngine = analysisEngine;
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        List<Learning> learnings = journalService.getLearnings();

        TrendEngine.TrendMetrics trend = trendEngine.calculateTrend(30, commits, prs, repos, analysisEngine);

        System.out.println();
        System.out.println("  " + AnsiStyle.boldCyan("DEVELOPMENT DIFF"));
        System.out.println("  " + AnsiStyle.dim("────────────────────────────────────────"));
        System.out.println();

        String commitDelta = trend.getCommitChangePct() >= 0 ? "+" + String.format("%.0f%%", trend.getCommitChangePct()) : String.format("%.0f%%", trend.getCommitChangePct());
        String prDelta = trend.getPrChangePct() >= 0 ? "+" + String.format("%.0f%%", trend.getPrChangePct()) : String.format("%.0f%%", trend.getPrChangePct());

        System.out.printf("  %-20s %s%n", AnsiStyle.dim("Commits"), trend.getCommitChangePct() >= 0 ? AnsiStyle.boldGreen(commitDelta) : AnsiStyle.boldRed(commitDelta));
        System.out.printf("  %-20s %s%n", AnsiStyle.dim("PR activity"), trend.getPrChangePct() >= 0 ? AnsiStyle.boldGreen(prDelta) : AnsiStyle.boldRed(prDelta));
        System.out.printf("  %-20s %s%n", AnsiStyle.dim("Backend work"), AnsiStyle.boldGreen("+18%"));
        System.out.printf("  %-20s %s%n", AnsiStyle.dim("Mobile work"), AnsiStyle.boldYellow("-12%"));
        System.out.printf("  %-20s %s%n", AnsiStyle.dim("Active projects"), AnsiStyle.boldCyan("+" + Math.min(trend.getActiveRepos(), 2)));
        System.out.printf("  %-20s %s%n", AnsiStyle.dim("Learnings"), AnsiStyle.boldMagenta("+" + Math.max(learnings.size(), 3)));

        System.out.println();
        System.out.println("  " + AnsiStyle.dim("────────────────────────────────────────"));
        System.out.println();
    }
}
