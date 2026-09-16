package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.Learning;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "xp", description = "Track developer level and XP progression", mixinStandardHelpOptions = true)
public class XpCommand implements Runnable {

    private final LocalStorageService storageService;
    private final XpEngine xpEngine;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public XpCommand(LocalStorageService storageService, XpEngine xpEngine, JournalService journalService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.xpEngine = xpEngine;
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

        XpEngine.XpStatus status = xpEngine.calculateXpStatus(commits, prs, repos, learnings);

        System.out.println();

        List<String> xpLines = new java.util.ArrayList<>();
        int bars = (int) Math.round((status.getProgressPercentage() / 100.0) * 18);
        String barStr = "█".repeat(bars) + "░".repeat(Math.max(0, 18 - bars));

        xpLines.add(String.format("  %s %s   %s",
                AnsiStyle.dim(String.format("%-18s", "Level Progress")),
                AnsiStyle.cyan("[" + barStr + "]"),
                AnsiStyle.boldWhite(status.getProgressPercentage() + "%")));
        xpLines.add(String.format("  %s %s",
                AnsiStyle.dim(String.format("%-18s", "XP This Week")),
                AnsiStyle.boldYellow("+" + status.getWeeklyXp())));
        xpLines.add("");
        xpLines.add("  " + AnsiStyle.dim(String.format("%-22s", "ACTIVITY TYPE")) + AnsiStyle.dim("XP REWARD"));
        xpLines.add("  " + AnsiStyle.dim("──────────────────────────────────────────────────"));
        xpLines.add(String.format("  %s %s", AnsiStyle.boldWhite(String.format("%-22s", "Open Source PR")), AnsiStyle.boldGreen("+100 XP")));
        xpLines.add(String.format("  %s %s", AnsiStyle.boldWhite(String.format("%-22s", "PR Merged")), AnsiStyle.boldGreen("+50 XP")));
        xpLines.add(String.format("  %s %s", AnsiStyle.boldWhite(String.format("%-22s", "Repo Maintained")), AnsiStyle.boldGreen("+30 XP")));
        xpLines.add(String.format("  %s %s", AnsiStyle.boldWhite(String.format("%-22s", "Technical Learning")), AnsiStyle.boldGreen("+20 XP")));
        xpLines.add(String.format("  %s %s", AnsiStyle.boldWhite(String.format("%-22s", "Daily Commit")), AnsiStyle.boldGreen("+10 XP")));

        BoxRenderer.renderBox("DEVELOPER LEVEL " + status.getLevel() + " - PROGRESS & XP", xpLines, AnsiStyle.GREEN);
        System.out.println();
    }
}
