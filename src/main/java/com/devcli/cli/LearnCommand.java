package com.devcli.cli;

import com.devcli.model.Learning;
import com.devcli.service.*;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Command(name = "learn", description = "Log or view developer learnings and discoveries", mixinStandardHelpOptions = true)
public class LearnCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Learning title or discovery note to record")
    private String learningTitle;

    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public LearnCommand(JournalService journalService, AuthService authService, SyncService syncService) {
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        if (learningTitle != null && !learningTitle.trim().isEmpty()) {
            journalService.addLearning(learningTitle.trim(), "General", "Recorded via DevCLI");
            System.out.println(AnsiStyle.boldGreen("\n✓ Recorded learning: ") + AnsiStyle.brightWhite(learningTitle.trim()));
            System.out.println(AnsiStyle.dim("Keep expanding your knowledge base! 🧠\n"));
            return;
        }

        List<Learning> learnings = journalService.getLearnings();
        BoxRenderer.printBanner("DEVELOPER JOURNAL - LEARNINGS 🧠", learnings.size() + " discoveries recorded");

        if (learnings.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No learnings recorded yet. Run `devcli learn \"Your discovery here\"` to add one!"));
            System.out.println();
            return;
        }

        for (Learning l : learnings) {
            String dateStr = l.getCreatedAt() != null ? l.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Recently";
            System.out.println("  " + AnsiStyle.boldMagenta("🧠 " + l.getTitle()));
            System.out.println("     " + AnsiStyle.dim("Category: " + l.getCategory() + " • Logged: " + dateStr));
            if (l.getDescription() != null && !l.getDescription().isEmpty()) {
                System.out.println("     " + AnsiStyle.gray(l.getDescription()));
            }
            System.out.println();
        }
        System.out.println("  " + AnsiStyle.dim("Tip: Record a new entry anytime using `devcli learn \"<title>\"`\n"));
    }
}
