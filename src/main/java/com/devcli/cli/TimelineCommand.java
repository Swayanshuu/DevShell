package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.Repository;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Command(name = "timeline", description = "View chronological visualization of development history and milestones", mixinStandardHelpOptions = true)
public class TimelineCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public TimelineCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
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
        List<Repository> repos = storageService.getRepositories();

        BoxRenderer.printBanner("DEVELOPER TIMELINE 🗓️", "Activity milestones over recent weeks");

        if (commits.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No commit activity cached. Run `devcli sync` to update timeline data."));
            return;
        }

        Map<LocalDate, List<Commit>> grouped = commits.stream()
                .filter(c -> c.getDate() != null)
                .collect(Collectors.groupingBy(c -> c.getDate().toLocalDate()));

        LocalDate current = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = current.minusDays(i);
            String dateLabel = date.format(DateTimeFormatter.ofPattern("EEE, MMM dd"));
            List<Commit> dayCommits = grouped.get(date);

            if (dayCommits != null && !dayCommits.isEmpty()) {
                String dots = "● ".repeat(Math.min(dayCommits.size(), 8));
                System.out.println("  " + AnsiStyle.boldCyan(String.format("%-14s", dateLabel)) + " ┃ " + AnsiStyle.boldGreen(dots) + AnsiStyle.dim("(" + dayCommits.size() + " commits)"));
                for (Commit c : dayCommits) {
                    System.out.println("                 ┃   " + AnsiStyle.cyan(c.getRepoName()) + " → " + AnsiStyle.boldWhite(c.getShortMessage()));
                }
            } else {
                System.out.println("  " + AnsiStyle.dim(String.format("%-14s", dateLabel)) + " ┃ " + AnsiStyle.dim("○ (no commits recorded)"));
            }
            System.out.println("                 ┃");
        }
        System.out.println("  " + AnsiStyle.dim("               ▲"));
        System.out.println("  " + AnsiStyle.dim("       Start of timeline window\n"));
    }
}
