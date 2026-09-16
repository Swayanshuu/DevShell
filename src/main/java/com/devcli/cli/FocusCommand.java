package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.Learning;
import com.devcli.model.Repository;
import com.devcli.service.AnalysisEngine;
import com.devcli.service.AuthService;
import com.devcli.service.JournalService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "focus", description = "Data-driven daily focus recommendations", mixinStandardHelpOptions = true)
public class FocusCommand implements Runnable {

    private final LocalStorageService storageService;
    private final JournalService journalService;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public FocusCommand(LocalStorageService storageService, JournalService journalService, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.journalService = journalService;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<Learning> learnings = journalService.getLearnings();

        String buildingRepo = analysisEngine.getCurrentlyBuildingProject(repos, commits);

        System.out.println();

        List<String> focusLines = new java.util.ArrayList<>();
        if (buildingRepo != null && !buildingRepo.isEmpty() && !"None".equalsIgnoreCase(buildingRepo)) {
            focusLines.add(String.format("  %s %s", AnsiStyle.boldYellow(String.format("%-20s", "Active Focus")), AnsiStyle.boldWhite(buildingRepo)));
        } else {
            focusLines.add(String.format("  %s %s", AnsiStyle.boldYellow(String.format("%-20s", "Active Focus")), AnsiStyle.boldWhite("No active project detected")));
        }

        int openBugs = storageService.getBugs() != null ? storageService.getBugs().size() : 0;
        focusLines.add(String.format("  %s %s", AnsiStyle.boldCyan(String.format("%-20s", "Issue Tracker")), AnsiStyle.boldWhite(openBugs + " local bugs logged")));

        focusLines.add(String.format("  %s %s", AnsiStyle.boldMagenta(String.format("%-20s", "Knowledge Base")), AnsiStyle.boldWhite(learnings.size() + " learnings saved")));
        if (!learnings.isEmpty()) {
            focusLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-20s", "Latest Note")), AnsiStyle.italic(learnings.get(learnings.size() - 1).getTitle())));
        }

        BoxRenderer.renderBox("RECOMMENDED DAILY FOCUS", focusLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
