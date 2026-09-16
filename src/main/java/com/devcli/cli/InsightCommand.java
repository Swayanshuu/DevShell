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

        System.out.println();
        List<String> inLines = new java.util.ArrayList<>();

        if (insights == null || insights.isEmpty()) {
            inLines.add("  " + AnsiStyle.yellow("Not enough data to generate observations."));
        } else {
            for (Insight ins : insights) {
                inLines.add("  " + AnsiStyle.boldWhite(String.format("%-25s", ins.getTitle())) + " " + AnsiStyle.cyan(ins.getMetric()));
                inLines.add("    " + AnsiStyle.gray(ins.getDetail()));
                inLines.add("");
            }
        }

        BoxRenderer.renderBox("DEVELOPMENT INSIGHTS & OBSERVATIONS", inLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
