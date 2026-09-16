package com.devcli.cli;

import com.devcli.model.Goal;
import com.devcli.service.AuthService;
import com.devcli.service.GoalEngine;
import com.devcli.service.SyncService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Component
@Command(name = "goals", aliases = {"goal"}, description = "Create and track personal development goals", mixinStandardHelpOptions = true)
public class GoalCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Action (e.g., 'add') or goal title")
    private String action;

    @Parameters(index = "1..*", arity = "0..*", description = "Goal title if action is 'add'")
    private String[] goalTitleParts;

    private final GoalEngine goalEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public GoalCommand(GoalEngine goalEngine, AuthService authService, SyncService syncService) {
        this.goalEngine = goalEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        if ("add".equalsIgnoreCase(action) && goalTitleParts != null && goalTitleParts.length > 0) {
            String title = String.join(" ", goalTitleParts).trim();
            Goal g = goalEngine.addGoal(title);
            System.out.println(AnsiStyle.boldGreen("\n✓ Goal added: ") + AnsiStyle.boldWhite(g.getTitle()) + "\n");
            return;
        }

        List<Goal> goals = goalEngine.getGoals();

        System.out.println();
        List<String> goalLines = new java.util.ArrayList<>();

        for (Goal g : goals) {
            int pct = g.getProgressPercentage();
            int bars = (int) Math.round((pct / 100.0) * 16);
            String barStr = "█".repeat(bars) + "░".repeat(Math.max(0, 16 - bars));

            goalLines.add(String.format("  %s %s   %s",
                    AnsiStyle.boldWhite(String.format("%-22s", g.getTitle())),
                    AnsiStyle.cyan("[" + barStr + "]"),
                    AnsiStyle.boldYellow(String.format("%3d%%", pct))));
        }

        BoxRenderer.renderBox("DEVELOPER GOALS TRACKER (" + goals.size() + ")", goalLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
