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

        System.out.println();
        List<String> timeLines = new java.util.ArrayList<>();

        if (commits.isEmpty()) {
            timeLines.add("  " + AnsiStyle.dim("No commit activity cached. Run `devshell sync` to update timeline data."));
        } else {
            Map<LocalDate, List<Commit>> grouped = commits.stream()
                    .filter(c -> c.getDate() != null)
                    .collect(Collectors.groupingBy(c -> c.getDate().toLocalDate()));

            LocalDate current = LocalDate.now();

            for (int i = 0; i < 7; i++) {
                LocalDate date = current.minusDays(i);
                String dateLabel = date.format(DateTimeFormatter.ofPattern("EEE, MMM dd"));
                List<Commit> dayCommits = grouped.get(date);

                if (dayCommits != null && !dayCommits.isEmpty()) {
                    timeLines.add(String.format("  %s %s   %s",
                            AnsiStyle.boldCyan(String.format("%-14s", dateLabel)),
                            AnsiStyle.boldGreen(String.format("%-12s", dayCommits.size() + " commits")),
                            AnsiStyle.boldWhite(dayCommits.get(0).getRepoName() + " → " + dayCommits.get(0).getShortMessage())));
                } else {
                    timeLines.add(String.format("  %s %s",
                            AnsiStyle.dim(String.format("%-14s", dateLabel)),
                            AnsiStyle.dim("0 commits recorded")));
                }
            }
        }

        BoxRenderer.renderBox("DEVELOPER ACTIVITY TIMELINE STREAM", timeLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
