package com.devcli.cli;

import com.devcli.model.ActivityEvent;
import com.devcli.model.Commit;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Command(name = "activity", description = "Show timeline and feed of recent GitHub development activity", mixinStandardHelpOptions = true)
public class ActivityCommand implements Runnable {

    @Option(names = {"--today"}, description = "Filter activity for today only")
    private boolean today;

    @Option(names = {"--week"}, description = "Filter activity for the past week")
    private boolean week;

    @Option(names = {"--project"}, description = "Filter activity for a specific project")
    private String projectFilter;

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ActivityCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        List<Commit> commits = storageService.getCommits();
        List<ActivityEvent> events = storageService.getEvents();

        LocalDate now = LocalDate.now();

        // Filter commits
        List<Commit> filteredCommits = commits.stream()
                .filter(c -> {
                    if (projectFilter != null && !projectFilter.isEmpty()) {
                        if (!c.getRepoName().equalsIgnoreCase(projectFilter)) return false;
                    }
                    if (today && c.getDate() != null) {
                        return c.getDate().toLocalDate().equals(now);
                    }
                    if (week && c.getDate() != null) {
                        return c.getDate().toLocalDate().isAfter(now.minusDays(7));
                    }
                    return true;
                })
                .collect(Collectors.toList());

        System.out.println();
        List<String> actLines = new java.util.ArrayList<>();

        if (filteredCommits.isEmpty()) {
            actLines.add("  " + AnsiStyle.yellow("No recent commit activity matching specified filters."));
        } else {
            actLines.add("  " + AnsiStyle.dim(String.format("%-12s %-18s %-10s %s", "TIME", "REPO", "SHA", "MESSAGE")));
            actLines.add("  " + AnsiStyle.dim("────────────────────────────────────────────────────────────"));

            for (Commit c : filteredCommits) {
                String timeStr = c.getDate() != null ? c.getDate().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "--:--";
                String repoStr = c.getRepoName() != null ? c.getRepoName() : "repo";
                String shaStr = "[" + c.getShortSha() + "]";
                String msgStr = c.getShortMessage();

                actLines.add(String.format("  %s %s %s %s",
                        AnsiStyle.dim(String.format("%-12s", timeStr)),
                        AnsiStyle.boldCyan(String.format("%-18s", repoStr)),
                        AnsiStyle.boldYellow(String.format("%-10s", shaStr)),
                        AnsiStyle.boldWhite(msgStr)));
            }
        }

        BoxRenderer.renderBox("DEVELOPMENT ACTIVITY FEED (" + filteredCommits.size() + ")", actLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
