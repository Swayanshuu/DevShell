package com.devcli.cli;

import com.devcli.model.Bug;
import com.devcli.service.*;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Command(name = "bugs", description = "Track, log, and resolve local developer bugs and issues", mixinStandardHelpOptions = true)
public class BugsCommand implements Runnable {

    @Option(names = {"--add"}, description = "Add a new bug title")
    private String addTitle;

    @Option(names = {"--resolve"}, description = "Mark a bug as resolved by title or ID")
    private String resolveTitle;

    @Option(names = {"--project"}, description = "Specify project name for bug")
    private String project;

    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public BugsCommand(JournalService journalService, AuthService authService, SyncService syncService) {
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        if (addTitle != null && !addTitle.trim().isEmpty()) {
            journalService.addBug(addTitle.trim(), project != null ? project : "General", "HIGH", "Tracked via DevCLI");
            System.out.println(AnsiStyle.boldGreen("\n✓ Logged bug: ") + AnsiStyle.brightWhite(addTitle.trim()));
            System.out.println();
            return;
        }

        if (resolveTitle != null && !resolveTitle.trim().isEmpty()) {
            boolean success = journalService.resolveBug(resolveTitle.trim());
            if (success) {
                System.out.println(AnsiStyle.boldGreen("\n✓ Resolved bug: ") + AnsiStyle.brightWhite(resolveTitle.trim()) + " 🐛✨");
            } else {
                System.out.println(AnsiStyle.brightRed("\n✗ Could not find open bug matching: ") + resolveTitle);
            }
            System.out.println();
            return;
        }

        List<Bug> bugs = journalService.getBugs();
        BoxRenderer.printBanner("DEVELOPER BUG TRACKER 🐛", bugs.size() + " bugs tracked");

        if (bugs.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No active bugs tracked. Log one with `devcli bugs --add \"Bug title\"`"));
            System.out.println();
            return;
        }

        for (Bug b : bugs) {
            String statusBadge = "RESOLVED".equalsIgnoreCase(b.getStatus()) ? AnsiStyle.boldGreen("✓ RESOLVED") : AnsiStyle.boldYellow("● " + b.getStatus());
            String dateStr = b.getCreatedAt() != null ? b.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Recently";

            System.out.println("  " + statusBadge + "  " + AnsiStyle.boldWhite(b.getTitle()));
            System.out.println("     " + AnsiStyle.dim("Project: " + b.getProject() + " • Severity: " + b.getSeverity() + " • Logged: " + dateStr));
            if (b.getNotes() != null && !b.getNotes().isEmpty()) {
                System.out.println("     " + AnsiStyle.gray(b.getNotes()));
            }
            System.out.println();
        }
        System.out.println("  " + AnsiStyle.dim("Commands: `devcli bugs --add \"<title>\"` | `devcli bugs --resolve \"<title>\"`\n"));
    }
}
