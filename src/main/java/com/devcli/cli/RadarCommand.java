package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.AnalysisEngine;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "radar", description = "Overview of developer items requiring attention", mixinStandardHelpOptions = true)
public class RadarCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public RadarCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
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

        int streak = analysisEngine.calculateStreak(commits);
        long inactiveCount = repos.stream().filter(r -> r.getStatus() == Repository.Status.INACTIVE).count();
        long openPrs = prs != null ? prs.stream().filter(pr -> "OPEN".equalsIgnoreCase(pr.getState())).count() : 0;
        int bugCount = storageService.getBugs() != null ? storageService.getBugs().size() : 0;

        System.out.println();

        List<String> radarLines = new java.util.ArrayList<>();
        if (inactiveCount > 0) {
            radarLines.add("  " + AnsiStyle.boldYellow("! ") + AnsiStyle.brightWhite(String.format("%-22s", "Inactive Repos")) + AnsiStyle.boldYellow(inactiveCount + " repos detected"));
        } else {
            radarLines.add("  " + AnsiStyle.boldGreen("✓ ") + AnsiStyle.brightWhite(String.format("%-22s", "Repo Maintenance")) + AnsiStyle.boldGreen("All active"));
        }

        if (bugCount > 0) {
            radarLines.add("  " + AnsiStyle.boldYellow("! ") + AnsiStyle.brightWhite(String.format("%-22s", "Open Bugs")) + AnsiStyle.boldYellow(bugCount + " requiring resolution"));
        } else {
            radarLines.add("  " + AnsiStyle.boldGreen("✓ ") + AnsiStyle.brightWhite(String.format("%-22s", "Open Bugs")) + AnsiStyle.boldGreen("Zero open bugs"));
        }

        if (openPrs > 0) {
            radarLines.add("  " + AnsiStyle.boldCyan("→ ") + AnsiStyle.brightWhite(String.format("%-22s", "Pull Requests")) + AnsiStyle.boldCyan(openPrs + " pending review"));
        } else {
            radarLines.add("  " + AnsiStyle.dim("  ") + AnsiStyle.brightWhite(String.format("%-22s", "Pull Requests")) + AnsiStyle.dim("No open PRs"));
        }

        if (streak > 0) {
            radarLines.add("  " + AnsiStyle.boldGreen("✓ ") + AnsiStyle.brightWhite(String.format("%-22s", "Commit Streak")) + AnsiStyle.boldGreen(streak + " days active"));
        } else {
            radarLines.add("  " + AnsiStyle.dim("  ") + AnsiStyle.brightWhite(String.format("%-22s", "Commit Streak")) + AnsiStyle.dim("0 days — commit today!"));
        }

        BoxRenderer.renderBox("DEVELOPER RADAR & ATTENTION METRICS", radarLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
