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

@Component
@Command(name = "achievements", description = "View unlocked developer achievements and badges", mixinStandardHelpOptions = true)
public class AchievementsCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AchievementEngine achievementEngine;
    private final AnalysisEngine analysisEngine;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public AchievementsCommand(LocalStorageService storageService, AchievementEngine achievementEngine, AnalysisEngine analysisEngine, JournalService journalService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.achievementEngine = achievementEngine;
        this.analysisEngine = analysisEngine;
        this.journalService = journalService;
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
        List<Learning> learnings = journalService.getLearnings();
        List<Bug> bugs = journalService.getBugs();
        int streak = analysisEngine.calculateStreak(commits);

        List<Achievement> achievements = achievementEngine.evaluateAchievements(repos, commits, prs, learnings, bugs, streak);

        long unlockedCount = achievements.stream().filter(Achievement::isUnlocked).count();

        List<String> achLines = new java.util.ArrayList<>();
        for (Achievement ach : achievements) {
            String title = ach.isUnlocked() ? AnsiStyle.boldGreen(String.format("%-22s", ach.getTitle())) : AnsiStyle.dim(String.format("%-22s", ach.getTitle()));
            String statusTag = ach.isUnlocked() ? AnsiStyle.boldGreen("UNLOCKED") : AnsiStyle.dim("LOCKED");

            achLines.add(String.format("  %s   %s   %s", title, statusTag, AnsiStyle.gray(ach.getProgressText())));
        }

        BoxRenderer.renderBox("DEVELOPER ACHIEVEMENTS (" + unlockedCount + " / " + achievements.size() + " UNLOCKED)", achLines, AnsiStyle.YELLOW);
        System.out.println();
    }
}
