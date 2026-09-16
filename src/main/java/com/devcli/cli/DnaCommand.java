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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Command(name = "dna", aliases = {"profile"}, description = "Analyze your complete developer DNA and tech identity", mixinStandardHelpOptions = true)
public class DnaCommand implements Runnable {

    private final LocalStorageService storageService;
    private final DeveloperDnaEngine dnaEngine;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public DnaCommand(LocalStorageService storageService, DeveloperDnaEngine dnaEngine, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.dnaEngine = dnaEngine;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();

        DeveloperDnaEngine.DnaProfile profile = dnaEngine.analyzeDna(repos, commits, prs, analysisEngine);

        System.out.println();
        
        List<String> categoryLines = new ArrayList<>();
        for (Map.Entry<String, Double> entry : profile.getCategoryBreakdown().entrySet()) {
            double pct = entry.getValue();
            int bars = (int) Math.round((pct / 100.0) * 15);
            String barStr = "█".repeat(bars) + "░".repeat(Math.max(0, 15 - bars));
            categoryLines.add(String.format("  %s %s   %s",
                    AnsiStyle.boldWhite(String.format("%-14s", entry.getKey())),
                    AnsiStyle.cyan("[" + barStr + "]"),
                    AnsiStyle.boldYellow(String.format("%3.0f%%", pct))));
        }
        BoxRenderer.renderBox("DEVELOPER DNA - TECH STACK", categoryLines, AnsiStyle.CYAN);
        System.out.println();

        List<String> metaLines = new ArrayList<>();
        metaLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Primary Domain")), AnsiStyle.boldGreen(profile.getPrimaryDomain())));
        metaLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Peak Productivity")), AnsiStyle.boldCyan(profile.getPeakProductivityWindow())));
        metaLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Longest Streak")), AnsiStyle.boldYellow(profile.getLongestStreakDays() + " days")));
        metaLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Public Repositories")), AnsiStyle.boldWhite(String.valueOf(profile.getTotalPublicRepos()))));
        metaLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Pull Requests")), AnsiStyle.boldMagenta(String.valueOf(profile.getTotalPrs()))));
        
        BoxRenderer.renderBox("DEVELOPER DNA - WORKFLOW", metaLines, AnsiStyle.GREEN);
        System.out.println();
    }
}
