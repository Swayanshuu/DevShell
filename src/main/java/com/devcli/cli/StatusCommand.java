package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Command(name = "status", description = "Show your Personal Developer Command Center snapshot", mixinStandardHelpOptions = true)
public class StatusCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final InsightEngine insightEngine;
    private final JournalService journalService;
    private final SyncService syncService;
    private final AuthService authService;

    @Autowired
    public StatusCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, InsightEngine insightEngine,
            JournalService journalService, SyncService syncService, AuthService authService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.insightEngine = insightEngine;
        this.journalService = journalService;
        this.syncService = syncService;
        this.authService = authService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        UserProfile user = storageService.getUserProfile();
        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        List<Learning> learnings = journalService.getLearnings();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);

        if (user == null) {
            System.out.println(AnsiStyle.brightRed("✗ No profile found. Run `devshell login` to authorize."));
            return;
        }

        // 1. ASCII Banner
        BoxRenderer.printAsciiBanner();

        // 2. Greeting Header
        String greeting = getGreeting();
        System.out.println("  " + greeting + ", " + AnsiStyle.boldCyan("@" + user.getUsername()) + " 👋");
        System.out.println("  " + AnsiStyle.dim("Developer Command Center v" + com.devcli.service.UpdateCheckerService.getCurrentVersion() + " • Session Active") + "\n");

        // 3. Metric Badges Row
        int streak = analysisEngine.calculateStreak(commits);
        List<Commit> todayCommits = getTodayCommits(commits);
        long activeProjectsCount = repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE).count();

        System.out.println("  🔥 " + AnsiStyle.boldYellow(streak + " day streak") + "    " +
                "💻 " + AnsiStyle.boldCyan(todayCommits.size() + " commits today") + "    " +
                "🚀 " + AnsiStyle.boldGreen(activeProjectsCount + " active projects") + "    " +
                "🧠 " + AnsiStyle.boldMagenta(learnings.size() + " journal entries"));
        System.out.println();

        // 4. Currently Building & Top Language Pills
        String currentlyBuilding = analysisEngine.getCurrentlyBuildingProject(repos, commits);
        System.out.println("  " + AnsiStyle.boldWhite("⚡ Currently Building"));
        System.out.println("  " + AnsiStyle.brightCyan("→ " + currentlyBuilding));

        if (!languages.isEmpty()) {
            StringBuilder langPills = new StringBuilder("  " + AnsiStyle.boldWhite("🧬 Top Tech Stack: "));
            int limit = 0;
            for (Map.Entry<String, Double> entry : languages.entrySet()) {
                if (limit >= 3)
                    break;
                langPills.append(AnsiStyle.cyan(entry.getKey()))
                        .append(" ")
                        .append(AnsiStyle.gray(String.format("(%.1f%%)", entry.getValue())))
                        .append("  ");
                limit++;
            }
            System.out.println(langPills.toString());
        }
        System.out.println();

        // 5. Commits of the Day Feed (Displays ALL commits pushed today)
        System.out.println("  " + AnsiStyle.boldWhite("⚡ Commits of the Day (" + todayCommits.size() + " total)"));
        System.out.println("  " + AnsiStyle.dim("─────────────────────────────────────────────────────────────"));

        if (!todayCommits.isEmpty()) {
            int displayCount = Math.min(todayCommits.size(), 7);
            for (int i = 0; i < displayCount; i++) {
                Commit c = todayCommits.get(i);
                String timeStr = c.getDate() != null ? c.getDate().format(DateTimeFormatter.ofPattern("HH:mm"))
                        : "--:--";
                String repoBadge = String.format("[%-14s]", truncate(c.getRepoName(), 14));
                String msg = truncate(c.getShortMessage(), 45);

                System.out.println("  " + AnsiStyle.dim(timeStr) + "  " +
                        AnsiStyle.cyan(repoBadge) + "  " +
                        AnsiStyle.brightWhite(msg));
            }
            if (todayCommits.size() > 7) {
                System.out.println(
                        "  " + AnsiStyle.gray("  ... and " + (todayCommits.size() - 7) + " more commits today"));
            }
        } else {
            System.out.println(
                    "  " + AnsiStyle.yellow("  No commits pushed yet today. Pick a project and ship code! 🚀"));
            if (!commits.isEmpty()) {
                System.out.println("\n  " + AnsiStyle.dim("  Recent Repository Activity:"));
                int recentLimit = Math.min(commits.size(), 3);
                for (int i = 0; i < recentLimit; i++) {
                    Commit c = commits.get(i);
                    String dateStr = c.getDate() != null
                            ? c.getDate().format(DateTimeFormatter.ofPattern("MMM dd HH:mm"))
                            : "Recent";
                    String repoBadge = String.format("[%-14s]", truncate(c.getRepoName(), 14));
                    System.out.println("  " + AnsiStyle.dim(dateStr) + "  " +
                            AnsiStyle.cyan(repoBadge) + "  " +
                            AnsiStyle.brightWhite(truncate(c.getShortMessage(), 40)));
                }
            }
        }
        System.out.println();

        // 6. 7-Day Velocity Sparkline
        renderWeeklySparkline(commits);

        // 7. System Observation Highlight
        BoxRenderer.printDivider();
        System.out.println("\n  " + AnsiStyle.boldMagenta("DEVSHELL OBSERVATION"));
        System.out.println("  " + AnsiStyle.dim("─────────────────────────────────────────"));
        String observation = insightEngine.getRandomObservation(repos, commits);
        System.out.println("  " + observation);
        System.out.println();

        String authorLink = AnsiStyle.hyperlink("@swayanshuu", "https://github.com/Swayanshuu");
        String swynxLink = AnsiStyle.hyperlink("SWYNX", "https://swynx.dev");
        System.out.println("  " + AnsiStyle.dim("Developed with ❤️ by ") + AnsiStyle.boldCyan(authorLink) + AnsiStyle.dim(" • Powered by ") + AnsiStyle.boldCyan(swynxLink));
        System.out.println(AnsiStyle.dim("  Type `devshell --help` to explore commands.\n"));
    }

    private List<Commit> getTodayCommits(List<Commit> commits) {
        LocalDate today = LocalDate.now();
        List<Commit> list = new ArrayList<>();
        for (Commit c : commits) {
            if (c.getDate() != null && c.getDate().toLocalDate().equals(today)) {
                list.add(c);
            }
        }
        return list;
    }

    private void renderWeeklySparkline(List<Commit> commits) {
        System.out.println("  " + AnsiStyle.boldWhite("📊 7-Day Commit Velocity"));
        LocalDate today = LocalDate.now();
        int maxCount = 1;
        int[] dayCounts = new int[7];
        String[] dayLabels = new String[7];
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("EEE (MM/dd)");

        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            int idx = 6 - i;
            dayLabels[idx] = targetDate.format(labelFmt);
            int count = 0;
            for (Commit c : commits) {
                if (c.getDate() != null && c.getDate().toLocalDate().equals(targetDate)) {
                    count++;
                }
            }
            dayCounts[idx] = count;
            if (count > maxCount)
                maxCount = count;
        }

        for (int i = 0; i < 7; i++) {
            int count = dayCounts[i];
            int barLength = (int) Math.round(((double) count / maxCount) * 15);
            if (count > 0 && barLength == 0)
                barLength = 1;

            String bar = "■".repeat(barLength);
            String empty = "·".repeat(15 - barLength);

            String line = String.format("  %-12s  %s%s  %s",
                    dayLabels[i],
                    count > 0 ? AnsiStyle.boldGreen(bar) : "",
                    AnsiStyle.dim(empty),
                    count > 0 ? AnsiStyle.boldCyan(count + " commits") : AnsiStyle.dim("0"));
            System.out.println(line);
        }
        System.out.println();
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 5 && hour < 12)
            return "Good morning";
        if (hour >= 12 && hour < 17)
            return "Good afternoon";
        return "Good evening";
    }

    private String truncate(String text, int maxLen) {
        if (text == null)
            return "";
        if (text.length() <= maxLen)
            return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
