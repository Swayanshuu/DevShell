package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

@Component
@Command(name = "insight", aliases = {"insights"}, description = "View intelligent, data-driven observations about your development patterns", mixinStandardHelpOptions = true)
public class InsightCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final InsightEngine insightEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public InsightCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, InsightEngine insightEngine, AuthService authService, SyncService syncService) {
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

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);

        List<Insight> insights = insightEngine.generateInsights(repos, commits, prs, languages);

        BoxRenderer.printBanner("DEVCLI INSIGHTS 💡", "Derived observations from your real development history");

        if (insights.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No insights available. Sync more activity data with `devcli sync`."));
            return;
        }

        for (Insight ins : insights) {
            String categoryBadge = AnsiStyle.boldMagenta("[" + ins.getCategory() + "]");
            System.out.println("  " + categoryBadge + "  " + AnsiStyle.boldWhite(ins.getTitle()));
            System.out.println("  " + AnsiStyle.gray(ins.getDetail()));
            System.out.println("  " + AnsiStyle.cyan("Metric Evidence: ") + AnsiStyle.brightCyan(ins.getMetric()));
            System.out.println();
        }
    }
}
