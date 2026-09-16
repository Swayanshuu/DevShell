package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Command(name = "history", description = "Build timeline from actual historical development data", mixinStandardHelpOptions = true)
public class HistoryCommand implements Runnable {

    @Option(names = {"--year"}, description = "Target year to inspect timeline (e.g. 2026)")
    private Integer targetYear;

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public HistoryCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        int year = targetYear != null ? targetYear : LocalDate.now().getYear();
        List<Commit> commits = storageService.getCommits();

        System.out.println();

        List<Commit> filtered = commits.stream()
                .filter(c -> c.getDate() != null && c.getDate().getYear() == year)
                .collect(Collectors.toList());

        List<String> historyLines = new java.util.ArrayList<>();

        if (filtered.isEmpty()) {
            historyLines.add("  " + AnsiStyle.dim("No commit activity recorded for " + year + "."));
            historyLines.add("  " + AnsiStyle.gray("Run devshell sync to sync latest commits from GitHub."));
        } else {
            Map<String, Map<String, Long>> monthlyMap = filtered.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            Collectors.groupingBy(
                                    c -> c.getRepoName() != null ? c.getRepoName() : "repo",
                                    Collectors.counting()
                            )
                    ));

            monthlyMap.forEach((month, repoMap) -> {
                repoMap.forEach((repo, count) -> {
                    int barLength = Math.min(15, Math.max(1, count.intValue()));
                    String bar = "█".repeat(barLength) + "░".repeat(Math.max(0, 15 - barLength));
                    historyLines.add(String.format("  %s %s %s   %s",
                            AnsiStyle.dim(String.format("%-5s", month)),
                            AnsiStyle.boldWhite(String.format("%-18s", repo)),
                            AnsiStyle.cyan("[" + bar + "]"),
                            AnsiStyle.boldYellow(count + " commits")));
                });
            });
        }

        BoxRenderer.renderBox("DEVELOPER TIMELINE (" + year + ")", historyLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
